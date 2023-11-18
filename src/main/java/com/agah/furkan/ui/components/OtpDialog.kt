package com.agah.furkan.ui.components

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OTPDialog(
    showDialog: MutableState<Boolean>,
    dialogProperties: DialogProperties = DialogProperties(),
    onDismiss: () -> Unit
) {
    val otpValue by remember { mutableStateOf(arrayOf("", "", "", "", "", "")) }
    val focusRequesters = remember {
        List(otpValue.size) { FocusRequester() }
    }
    val currentFocus = remember { mutableStateOf(0) }

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
                repeat(otpValue.size) { index ->
                    var digit by remember {
                        mutableStateOf(otpValue.getOrNull(index).orEmpty())
                    }

                    SimpleTextField(
                        value = digit,
                        onValueChange = {
                            if (it.isNotEmpty() && index < 5) {
                                focusRequesters[index + 1]
                            } else {
                                if (it.isEmpty() && index > 0) {
                                    focusRequesters[index - 1]
                                } else {
                                    null
                                }?.let { focusRequester ->
                                    /**
                                     * workarond for java.lang.IllegalStateException: Required value was null.
                                     * exception when value pasted from clipboard
                                     */
                                    try {
                                        focusRequester.requestFocus()
                                    } catch (ex: IllegalStateException) {
                                        // no-op
                                    }
                                }
                            }

                            digit = it.take(1)
                            otpValue[index] = it.take(1)
                        },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (!otpValue.any { it.isEmpty() }) {
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
                            .onFocusChanged {
                                if (it.isCaptured) {
                                    currentFocus.value = index
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
                    if (!otpValue.any { it.isEmpty() }) {
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
