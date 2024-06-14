package com.agah.furkan.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.window.PopupProperties

@Composable
fun AutoCompleteTextView(
    modifier: Modifier = Modifier,
    onValueChange: (TextFieldValue) -> Unit = {},
    options: List<String>,
    onItemSelected: (String) -> Unit = {}
) {
    val textFieldValue = remember {
        mutableStateOf(TextFieldValue())
    }
    val dropDownExpanded = remember {
        mutableStateOf(false)
    }

    val dropDownOptions = remember {
        mutableStateOf(listOf<String>())
    }

    Box(modifier) {
        TextField(
            modifier = Modifier
                .fillMaxWidth(),
            value = textFieldValue.value,
            onValueChange = { value ->
                val filteredList =
                    options.filter {
                        it.contains(
                            value.text,
                            ignoreCase = true
                        ) && value.text != it
                    }
                        .take(3)
                dropDownExpanded.value = filteredList.isNotEmpty()
                dropDownOptions.value = filteredList
                textFieldValue.value = value
                onValueChange(value)
            },
            colors = OutlinedTextFieldDefaults.colors()
        )
        DropdownMenu(
            expanded = dropDownExpanded.value,
            properties = PopupProperties(
                focusable = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            ),
            onDismissRequest = {
                dropDownExpanded.value = false
            }
        ) {
            dropDownOptions.value.forEach { text ->
                DropdownMenuItem(onClick = {
                    textFieldValue.value = TextFieldValue(
                        text = text,
                        selection = TextRange(text.length)
                    )
                    onItemSelected(text)
                }, text = { Text(text = text) })
            }
        }
    }
}
