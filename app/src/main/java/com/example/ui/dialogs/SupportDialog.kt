package com.example.ui.dialogs

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.localization.Language

@Composable
fun SupportDialog(
    isDarkTheme: Boolean = true,
    currentLanguage: Language = Language.EN,
    username: String = "",
    currentLevel: Int = 1,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val isTr = currentLanguage == Language.TR

    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var emailAddress by remember { mutableStateOf("") }
    var messageText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val bgColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFFFFFFF)
    val cardBg = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFF8FAFC)
    val textPrimary = if (isDarkTheme) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val textSecondary = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)
    val borderColor = if (isDarkTheme) Color(0xFF334155) else Color(0xFFE2E8F0)
    val primaryCyan = Color(0xFF00E5FF)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .padding(16.dp)
                .widthIn(max = 440.dp)
                .fillMaxWidth()
                .imePadding()
                .shadow(24.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, borderColor, RoundedCornerShape(24.dp)),
            color = bgColor
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with icon and Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(primaryCyan.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SupportAgent,
                                contentDescription = "Destek",
                                tint = primaryCyan,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isTr) "Destek & İletişim" else "Support & Contact",
                                color = textPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "atagulgamesdestek@gmail.com",
                                color = primaryCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("support_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (isTr)
                        "Geri bildiriminiz, önerileriniz veya karşılaştığınız sorunlar için bize ulaşın. Ekibimiz en kısa sürede dönüş yapacaktır."
                    else
                        "Contact us for your feedback, suggestions or any issues. Our support team will get back to you promptly.",
                    color = textSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Adı Soyadı Input
                OutlinedTextField(
                    value = fullName,
                    onValueChange = {
                        fullName = it
                        errorMessage = null
                    },
                    label = { Text(if (isTr) "Ad Soyad *" else "Full Name *") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = primaryCyan
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("support_fullname_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryCyan,
                        unfocusedBorderColor = borderColor,
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary,
                        focusedContainerColor = cardBg,
                        unfocusedContainerColor = cardBg
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Telefon Numarası Input
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = {
                        phoneNumber = it
                        errorMessage = null
                    },
                    label = { Text(if (isTr) "Telefon Numarası *" else "Phone Number *") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = primaryCyan
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("support_phone_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryCyan,
                        unfocusedBorderColor = borderColor,
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary,
                        focusedContainerColor = cardBg,
                        unfocusedContainerColor = cardBg
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // E-posta Input
                OutlinedTextField(
                    value = emailAddress,
                    onValueChange = {
                        emailAddress = it
                        errorMessage = null
                    },
                    label = { Text(if (isTr) "E-posta Adresiniz *" else "Your Email *") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = primaryCyan
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("support_email_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryCyan,
                        unfocusedBorderColor = borderColor,
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary,
                        focusedContainerColor = cardBg,
                        unfocusedContainerColor = cardBg
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Mesaj Input
                OutlinedTextField(
                    value = messageText,
                    onValueChange = {
                        messageText = it
                        errorMessage = null
                    },
                    label = { Text(if (isTr) "Mesajınız / Sorun Detayı *" else "Your Message / Issue *") },
                    minLines = 3,
                    maxLines = 5,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("support_message_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryCyan,
                        unfocusedBorderColor = borderColor,
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary,
                        focusedContainerColor = cardBg,
                        unfocusedContainerColor = cardBg
                    )
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFEF4444),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Send Button
                Button(
                    onClick = {
                        if (fullName.trim().isEmpty()) {
                            errorMessage = if (isTr) "Lütfen ad ve soyadınızı girin." else "Please enter your full name."
                            return@Button
                        }
                        if (phoneNumber.trim().isEmpty()) {
                            errorMessage = if (isTr) "Lütfen telefon numaranızı girin." else "Please enter your phone number."
                            return@Button
                        }
                        if (emailAddress.trim().isEmpty() || !emailAddress.contains("@")) {
                            errorMessage = if (isTr) "Lütfen geçerli bir e-posta adresi girin." else "Please enter a valid email."
                            return@Button
                        }
                        if (messageText.trim().isEmpty()) {
                            errorMessage = if (isTr) "Lütfen mesajınızı yazın." else "Please enter your message."
                            return@Button
                        }

                        sendSupportEmail(
                            context = context,
                            fullName = fullName.trim(),
                            phoneNumber = phoneNumber.trim(),
                            email = emailAddress.trim(),
                            message = messageText.trim(),
                            username = username,
                            level = currentLevel,
                            isTr = isTr
                        )
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("support_submit_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryCyan,
                        contentColor = Color(0xFF0F172A)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isTr) "E-posta Gönder" else "Send Email",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun sendSupportEmail(
    context: Context,
    fullName: String,
    phoneNumber: String,
    email: String,
    message: String,
    username: String,
    level: Int,
    isTr: Boolean
) {
    val recipient = "atagulgamesdestek@gmail.com"
    val subject = "[ECHO Destek] $fullName - Seviye $level"
    val emailBody = buildString {
        appendLine("--- ECHO DESTEK TALEBİ ---")
        appendLine("Ad Soyad: $fullName")
        appendLine("Telefon: $phoneNumber")
        appendLine("E-posta: $email")
        if (username.isNotEmpty()) {
            appendLine("Oyuncu Adı: $username")
        }
        appendLine("Mevcut Seviye: $level")
        appendLine("Tarih: ${java.util.Date()}")
        appendLine("--------------------------")
        appendLine()
        appendLine("Mesaj:")
        appendLine(message)
    }

    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:$recipient")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, emailBody)
    }

    try {
        context.startActivity(Intent.createChooser(intent, if (isTr) "E-posta Uygulaması Seçin" else "Choose Email Client"))
    } catch (e: Exception) {
        Toast.makeText(
            context,
            if (isTr) "E-posta uygulaması bulunamadı: $recipient" else "No email app found: $recipient",
            Toast.LENGTH_LONG
        ).show()
    }
}
