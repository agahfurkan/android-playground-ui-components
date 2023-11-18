package com.agah.furkan.ui.components

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.agah.furkan.AppSignatureHelper
import com.agah.furkan.SmsBroadcastReceiver
import com.google.android.gms.auth.api.phone.SmsRetriever

private const val OTP_LENGTH = 6

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun OTPDialog(
    showDialog: MutableState<Boolean>,
    dialogProperties: DialogProperties = DialogProperties(),
    onDismiss: () -> Unit
) {
    var otpValue by remember { mutableStateOf(arrayOf("", "", "", "", "", "")) }
    val focusRequesters = remember {
        List(OTP_LENGTH) { FocusRequester() }
    }
    val context = LocalContext.current as Activity
    AppSignatureHelper(context).appSignatures.forEach {
        // get app hash key for sms
    }
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK && it.data != null) {
                val message = it.data?.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
                // used sms format is 'Your otp code is: 123456'
                val otpCode = message?.substringAfterLast(": ")?.substringBeforeLast(" ")

                otpValue = otpValue.copyOf().apply {
                    otpCode?.forEachIndexed { index, char ->
                        this[index] = char.toString()
                    }
                }
            }
        }
    DisposableEffect(Unit) {
        val client = SmsRetriever.getClient(context)

        // for automatic sms verification sample
        /*val task = client.startSmsRetriever()
        task.addOnSuccessListener {
            // no-op
        }
        task.addOnFailureListener {
            // no-op
        }*/

        client.startSmsUserConsent(null)
        val broadcastReceiver = SmsBroadcastReceiver()
        broadcastReceiver.setListener(object : SmsBroadcastReceiver.SmsListener {
            override fun onSmsReceived(message: String) {
                // extract 6 digit code from message
                // automatic sms verification sample
            }

            override fun onSmsTimeOut() {
                // no-op
            }

            // for one time consent sample
            override fun startActivityForResult(consentIntent: Intent?) {
                consentIntent?.let {
                    launcher.launch(it)
                }
            }
        })
        val filter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        context.registerReceiver(broadcastReceiver, filter)

        onDispose {
            context.unregisterReceiver(broadcastReceiver)
        }
    }

    Dialog(
        onDismissRequest = { showDialog.value = false },
        properties = dialogProperties
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(Color.White)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(id = R.string.enter_otp),
                fontSize = 20.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                repeat(OTP_LENGTH) { index ->
                    SimpleTextField(
                        value = otpValue[index],
                        onValueChange = {
                            /**
                             * workaround for java.lang.IllegalStateException: Required value was null.
                             * exception occurs when value pasted from clipboard
                             */
                            try {
                                if (index < OTP_LENGTH - 1) {
                                    focusRequesters[index + 1].requestFocus()
                                }
                            } catch (ex: IllegalStateException) {
                                // no-op
                            }

                            otpValue = otpValue.copyOf().apply {
                                this[index] = it.take(1)
                            }
                        },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (otpValue.isNotEmpty()) {
                                    onDismiss()
                                }
                            }
                        ),
                        modifier = Modifier
                            .padding(2.dp)
                            .weight(1f)
                            .aspectRatio(1f)
                            .background(
                                color = Color.LightGray,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .focusRequester(focusRequesters[index])
                            .onKeyEvent {
                                if (it.key == Key.Backspace) {
                                    if (index > 0) {
                                        focusRequesters[index - 1].requestFocus()
                                    }
                                    true
                                } else {
                                    false
                                }
                            },
                        textStyle = androidx.compose.ui.text.TextStyle.Default.copy(
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center
                        ),
                        cursorBrush = SolidColor(Transparent),
                    )
                }
            }

            Button(
                onClick = {
                    if (otpValue.isNotEmpty()) {
                        onDismiss()
                    }
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(text = stringResource(id = R.string.ok))
            }
        }

        BackHandler {
            onDismiss()
        }
    }

    BackHandler {
        onDismiss()
    }
}

@SuppressLint("UnrememberedMutableState")
@Composable
@Preview
fun OtpDialogPreview() {
    OTPDialog(showDialog = mutableStateOf(true), onDismiss = {})
}
