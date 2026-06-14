package com.dopey.timeawarenessapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dopey.timeawarenessapp.ui.theme.TerminalBackground
import com.dopey.timeawarenessapp.ui.theme.TerminalGreen
import com.dopey.timeawarenessapp.ui.theme.TerminalGray

@Composable
fun EarlyClockInDialog(
    nextTargetHour: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TerminalBackground,
        shape = RoundedCornerShape(8.dp),
        title = {
            Text(
                "CLOCK IN EARLY?",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = TerminalGreen,
                letterSpacing = 2.sp
            )
        },
        text = {
            Text(
                "You're pressing before %02d:00.\nDo you want to clock in for that target now anyway?".format(nextTargetHour),
                fontFamily = FontFamily.Monospace,
                color = TerminalGray,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("YES, CLOCK IN", fontFamily = FontFamily.Monospace, color = TerminalGreen)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("MISTAKE", fontFamily = FontFamily.Monospace, color = TerminalGray)
            }
        }
    )
}

@Composable
fun AllDoneDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TerminalBackground,
        shape = RoundedCornerShape(8.dp),
        title = {
            Text(
                "ALL DONE!",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = TerminalGreen,
                letterSpacing = 2.sp
            )
        },
        text = {
            Text(
                "You have already checked off all possible targets for today.",
                fontFamily = FontFamily.Monospace,
                color = TerminalGray,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK", fontFamily = FontFamily.Monospace, color = TerminalGreen)
            }
        }
    )
}
