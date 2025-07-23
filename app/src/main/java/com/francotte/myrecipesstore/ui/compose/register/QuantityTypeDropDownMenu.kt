package com.francotte.myrecipesstore.ui.compose.register

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeasurementUnitDropDownMenu(
    modifier: Modifier = Modifier,
    onMeasureSelected: (MeasurementUnit) -> Unit
) {
    val list = enumValues<MeasurementUnit>()
    var text by remember { mutableStateOf("Type") }
    var expanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val focusManager = LocalFocusManager.current
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect {
            if (it is FocusInteraction.Focus) {
                focusManager.clearFocus()
            }
        }
    }
    ExposedDropdownMenuBox(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF6E8D6)),
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }) {
        BasicTextField(
            value = text,
            onValueChange = {},
            readOnly = true,
            interactionSource = interactionSource,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                .focusable(false)
                .background(Color(0xFFF6E8D6), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 12.dp),
            textStyle = LocalTextStyle.current.copy(color = Color(0xFF6D4C41)),
            decorationBox = { innerTextField ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(Modifier.weight(1f)) {
                        if (text.isEmpty()) {
                            Text(
                                text = "Type",
                                textAlign = TextAlign.Center,
                                color = Color(0xFF6D4C41),
                                fontSize = 12.sp
                            )
                        }
                        innerTextField()
                    }
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color(0xFF6D4C41)
                    )
                }
            }
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            list.forEachIndexed { index, measureType ->
                DropdownMenuItem(
                    text = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = measureType.toString(),
                                textAlign = TextAlign.Center
                            )
                        }
                    },
                    onClick = {
                        text = list[index].displayName
                        onMeasureSelected(list[index])
                        expanded = false
                    },
                    contentPadding = PaddingValues(vertical = 0.dp, horizontal = 8.dp))
            }
        }
    }
}


enum class MeasurementUnit(val displayName: String, val icon: String) {
    TABLESPOON("tbsp","\uD83C\uDF7D️"), // 🍽️
    TEASPOON("tsp","\u2615"),           // ☕ (symbol for hot beverage)
    MILLIGRAM("mg","\u2696"), // ⚖
    GRAM("g","\u2696"),
    KILOGRAM("kg","\u2696"),
    MILLILITER("ml","\uD83E\uDD64"), // 🥤
    LITER("l","\uD83E\uDD64"),// 🥛
    DASH("dash","\uD83E\uDDC2"),             // 🧂🍲
    SLICE("slice","\uD83E\uDDC0"),
    UNIT("piece",""); // 🔢

    override fun toString(): String = "$icon $displayName"
}
