package com.smartlogix.app.ui.screen.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartlogix.app.models.EstadoAprobacion
import com.smartlogix.app.models.Rol

/**
 * Badge para mostrar cantidad de stock
 * Colores según nivel de stock:
 * - Rojo: stock bajo (< 10)
 * - Naranja: stock medio (10-50)
 * - Verde: stock alto (> 50)
 */
@Composable
fun StockBadge(
    stock: Int,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = when {
        stock < 10 -> Color(0xFF4A1C1C) to Color(0xFFFF6B6B) // Rojo dark
        stock < 50 -> Color(0xFF4A3300) to Color(0xFFFFB74D) // Naranja dark
        else -> Color(0xFF1B3A1B) to Color(0xFF66BB6A) // Verde dark
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Text(
            text = "Stock: $stock",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Badge para mostrar el estado de una aprobación
 */
@Composable
fun EstadoBadge(
    estado: EstadoAprobacion,
    modifier: Modifier = Modifier
) {
    val backgroundColor: Color
    val textColor: Color
    val text: String

    when (estado) {
        EstadoAprobacion.PENDIENTE -> {
            backgroundColor = Color(0xFFFFF9C4)
            textColor = Color(0xFFF57F17)
            text = "PENDIENTE"
        }
        EstadoAprobacion.APROBADO -> {
            backgroundColor = Color(0xFFE8F5E9)
            textColor = Color(0xFF388E3C)
            text = "APROBADO"
        }
        EstadoAprobacion.RECHAZADO -> {
            backgroundColor = Color(0xFFFFEBEE)
            textColor = Color(0xFFD32F2F)
            text = "RECHAZADO"
        }
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Badge para mostrar el rol de un usuario
 */
@Composable
fun RolBadge(
    rol: Rol,
    modifier: Modifier = Modifier
) {
    val backgroundColor: Color
    val textColor: Color
    val text: String

    when (rol) {
        Rol.ADMIN -> {
            backgroundColor = Color(0xFFE1BEE7)
            textColor = Color(0xFF6A1B9A)
            text = "ADMIN"
        }
        Rol.JEFE -> {
            backgroundColor = Color(0xFFBBDEFB)
            textColor = Color(0xFF1976D2)
            text = "JEFE"
        }
        Rol.SUPERVISOR -> {
            backgroundColor = Color(0xFFC5E1A5)
            textColor = Color(0xFF558B2F)
            text = "SUPERVISOR"
        }
        Rol.OPERADOR -> {
            backgroundColor = Color(0xFFFFE0B2)
            textColor = Color(0xFFE65100)
            text = "OPERADOR"
        }
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Badge de alerta/importante
 */
@Composable
fun AlertBadge(
    text: String = "IMPORTANTE",
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFFF3E0)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⚠",
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFFF57C00),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Badge contador (para mostrar cantidad de elementos)
 * Ejemplo: cantidad de mensajes no leídos, solicitudes pendientes, etc.
 */
@Composable
fun CounterBadge(
    count: Int,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFFD32F2F),
    textColor: Color = Color.White
) {
    if (count > 0) {
        Surface(
            modifier = modifier
                .size(24.dp),
            shape = RoundedCornerShape(12.dp),
            color = backgroundColor
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = if (count > 99) "99+" else count.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }
        }
    }
}

/**
 * Chip seleccionable para filtros
 */
@Composable
fun FilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = text,
                color = if (selected)
                    com.smartlogix.app.ui.theme.VibrantBlue
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = com.smartlogix.app.ui.theme.NavyDashboard,
            selectedContainerColor = com.smartlogix.app.ui.theme.NavyDark
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = com.smartlogix.app.ui.theme.NavyDark,
            selectedBorderColor = com.smartlogix.app.ui.theme.VibrantBlue,
            enabled = true,
            selected = selected
        )
    )
}

/**
 * Badge de progreso con porcentaje
 */
@Composable
fun ProgressBadge(
    percentage: Double,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = when {
        percentage < 30 -> Color(0xFFFFEBEE) to Color(0xFFD32F2F)
        percentage < 70 -> Color(0xFFFFF3E0) to Color(0xFFF57C00)
        else -> Color(0xFFE8F5E9) to Color(0xFF388E3C)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Text(
            text = "${percentage.toInt()}%",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
    }
}

