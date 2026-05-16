package com.smartlogix.app.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.smartlogix.app.SmartLogixApplication
import com.smartlogix.app.R
import com.smartlogix.app.repository.PedidosRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object NotificationHelper {
    private const val CHANNEL_ID = "SmartLogix_pedidos_channel"
    private const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Pedidos y Alertas"
            val descriptionText = "Notificaciones de nuevos pedidos y asignaciones"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNewOrderNotification(context: Context, cantidad: Int) {
        // En Android 13+ se requiere permiso POST_NOTIFICATIONS, se asume manejado en UI
        try {
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_email)
                .setContentTitle("Nueva Nota de Venta")
                .setContentText("Hay $cantidad nuevos pedidos pendientes.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            with(NotificationManagerCompat.from(context)) {
                notify(NOTIFICATION_ID, builder.build())
            }
        } catch (e: SecurityException) {}
    }

    fun showPickingCompletedNotification(context: Context, pedidoId: Int) {
        try {
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Picking Completado")
                .setContentText("El picking de la NV #$pedidoId ha finalizado. Pendiente de validación.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            with(NotificationManagerCompat.from(context)) {
                notify(pedidoId, builder.build())
            }
        } catch (e: SecurityException) {}
    }

    fun showFacturadoNotification(context: Context, pedidoId: Int) {
        try {
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_agenda)
                .setContentTitle("NV Facturada")
                .setContentText("La NV #$pedidoId ha sido facturada. Pendiente de despacho.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            with(NotificationManagerCompat.from(context)) {
                notify(pedidoId + 10000, builder.build())
            }
        } catch (e: SecurityException) {}
    }
}

class PollingManager(private val repository: PedidosRepository) {
    private val _hasNewOrders = MutableStateFlow(false)
    val hasNewOrders: StateFlow<Boolean> = _hasNewOrders
    
    private val _pendingOrdersCount = MutableStateFlow(0)
    val pendingOrdersCount: StateFlow<Int> = _pendingOrdersCount

    private var pollingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun startPolling(context: Context) {
        if (pollingJob?.isActive == true) return
        
        NotificationHelper.createNotificationChannel(context)

        pollingJob = scope.launch {
            while (isActive) {
                checkPendingOrders(context)
                delay(2 * 60 * 1000L) // 2 minutos
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
    }

    private val _completedOrders = mutableSetOf<Int>()
    private val _facturadoOrders = mutableSetOf<Int>()

    private suspend fun checkPendingOrders(context: Context) {
        val app = SmartLogixApplication.instance
        val userRole = app.getCurrentUserRole()
        
        // 1. Chequear pedidos pendientes de asignación (Todos los roles pueden ver esto generalmente)
        val pendingResult = repository.getPedidosPendientes()
        pendingResult.onSuccess { pedidos ->
            val count = pedidos.size
            if (count > _pendingOrdersCount.value) {
                withContext(Dispatchers.Main) {
                    NotificationHelper.showNewOrderNotification(context, count)
                }
            }
            _pendingOrdersCount.emit(count)
            _hasNewOrders.emit(count > 0)
        }

        // 2. Chequear todos los pedidos para Jefe/Supervisor/Admin (para notificaciones de flujo)
        // Evitamos llamar si es OPERADOR para prevenir 403 si el backend lo restringe
        if (userRole != "OPERADOR") {
            val allResult = repository.getPedidosTodos()
            allResult.onSuccess { pedidos ->
                pedidos.forEach { pedido ->
                    when (pedido.estado) {
                        com.smartlogix.app.model.EstadoPedido.PICKING_COMPLETADO -> {
                            if (!_completedOrders.contains(pedido.id)) {
                                _completedOrders.add(pedido.id)
                                withContext(Dispatchers.Main) {
                                    NotificationHelper.showPickingCompletedNotification(context, pedido.id)
                                }
                            }
                        }
                        com.smartlogix.app.model.EstadoPedido.FACTURADO -> {
                            if (!_facturadoOrders.contains(pedido.id)) {
                                _facturadoOrders.add(pedido.id)
                                withContext(Dispatchers.Main) {
                                    NotificationHelper.showFacturadoNotification(context, pedido.id)
                                }
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}


