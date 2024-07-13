import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp


/**
 * Basic color picker
 * @param initialColor given start color to change
 * @param iconsColor tint color for color and alpha rectangle icons
 * @param shadeBoxSize size which will be applied to color box, color rect height and alpha rect width
 * @param colorRectWidth size which will be applied to color rect width and alpha rect height
 * @param boxCursorSize size for color picker box circle cursor
 * @param arrangement space between box and color and alpha rectangles
 * @param onColorChange return selected color
 * */
@Composable
fun ColorPicker(
    initialColor: Color = Color.Red,
    iconsColor: Color = Color.Black,
    shadeBoxSize: Int = 255,
    colorRectWidth: Int = 20,
    boxCursorSize: Int = 20,
    arrangement: Int = 20,
    onColorChange: (Color) -> Unit
) {

    val correctShadeBoxSize = if (shadeBoxSize > 0) shadeBoxSize else 255
    val correctColorRectWidth = if (colorRectWidth > 0) colorRectWidth else 20
    val correctCursorSize = if (boxCursorSize > 0) boxCursorSize else 20
    val correctArrangement = if (arrangement >= 0) arrangement else 20
    val density = LocalDensity.current.density
    val shadeBoxSizeWithDensity = correctShadeBoxSize * density
    

    var x by remember { mutableStateOf(getShadeCursorOffset(initialColor, shadeBoxSizeWithDensity.toInt()).x) }  // Shade box cursor position X
    var y by remember { mutableStateOf(getShadeCursorOffset(initialColor, shadeBoxSizeWithDensity.toInt()).y) }  // Shade box cursor position Y
    var colorPosY by remember { mutableStateOf(getColorCursorPosition(initialColor, shadeBoxSizeWithDensity.toInt())) }  // Color rectangle cursor position Y
    var alphaPosX by remember { mutableStateOf(getAlphaCursorPosition(initialColor, shadeBoxSizeWithDensity.toInt())) }  // Alpha rectangle cursor position X

    var color by remember { mutableStateOf(getShadeBoxBackground(initialColor)) }
    var colorShade by remember { mutableStateOf(getColorShade(color, Offset(x, y), shadeBoxSizeWithDensity.toInt())) }
    var resultColor by remember { mutableStateOf(colorShade) }

    LaunchedEffect(initialColor) {
        color = getShadeBoxBackground(initialColor)
        x = getShadeCursorOffset(initialColor, shadeBoxSizeWithDensity.toInt()).x
        y = getShadeCursorOffset(initialColor, shadeBoxSizeWithDensity.toInt()).y
        colorPosY = getColorCursorPosition(initialColor, shadeBoxSizeWithDensity.toInt())
        alphaPosX = getAlphaCursorPosition(initialColor, shadeBoxSizeWithDensity.toInt())
        colorShade = getColorShade(color, Offset(x, y), shadeBoxSizeWithDensity.toInt())
        resultColor = colorShade
    }  // Call if for some reason you want to change initial color right in changing process

    LaunchedEffect(color) { colorShade = getColorShade(color, Offset(x, y), shadeBoxSizeWithDensity.toInt()) }  // Call on RAW color change to change shade color

    LaunchedEffect(colorShade) {
        resultColor = colorShade.copy(alpha = getAlpha(Offset(alphaPosX, 0f), shadeBoxSizeWithDensity.toInt()))
        onColorChange(resultColor)  // return complete color
    }  // Call on color shade change to change result color

    LaunchedEffect(alphaPosX) { onColorChange(resultColor) }



    Column(verticalArrangement = Arrangement.spacedBy(correctArrangement.dp)) {

        Row(horizontalArrangement = Arrangement.spacedBy(correctArrangement.dp)) {

// Shade box
            Box(Modifier.clip(RectangleShape)) {
                Box(
                    Modifier
                        .size(correctShadeBoxSize.dp)
                        .background(brush = Brush.horizontalGradient( listOf(Color.White, color) ) )
                )

                Box(
                    Modifier
                        .size(correctShadeBoxSize.dp)
                        .background(brush = Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
                        .pointerInput(Unit) {
                            detectTapGestures {
                                val position = it
                                colorShade = getColorShade(color, position, shadeBoxSizeWithDensity.toInt())
                                x = position.x
                                y = position.y
                            }
                        }
                        .pointerInput(Unit) {
                            detectDragGestures { change, _ ->
                                x = (change.position.x).coerceIn(0f..shadeBoxSizeWithDensity)
                                y = (change.position.y).coerceIn(0f..shadeBoxSizeWithDensity)
                                colorShade = getColorShade(color, Offset(x, y), shadeBoxSizeWithDensity.toInt())
                            }

                        }
                )


// Shade picker cursor
                Box(
                    Modifier
                        .offset {
                            IntOffset(
                                x = x.toInt() - getCenter(correctCursorSize, density).x.toInt(),
                                y = y.toInt() - getCenter(correctCursorSize, density).y.toInt()
                            )
                        }
                        .size(correctCursorSize.dp)
                        .border(2.dp, Color.Gray, CircleShape)
                )
            }

// Vertical color rectangle
            Row {

                Icon(
                    painter = painterResource("arrow_right.png"),
                    contentDescription = null,
                    tint = iconsColor,
                    modifier = Modifier
                        .offset { IntOffset(0, colorPosY.toInt() - 5.dp.roundToPx()) }
                        .size(10.dp)
                )

                Box(
                    Modifier
                        .size(correctColorRectWidth.dp, correctShadeBoxSize.dp)
                        .background(brush = Brush.verticalGradient( listOf(
                            Color.Red, Color(1f, 0f, 1f),
                            Color.Blue, Color(0f, 1f, 1f),
                            Color.Green, Color(1f, 1f, 0f),
                            Color.Red
                        ), tileMode = TileMode.Repeated ) )
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    val position = it
                                    colorPosY = position.y
                                    color = getColor(color, position, shadeBoxSizeWithDensity.toInt())
                                }
                            )
                        }
                        .pointerInput(Unit) {
                            detectDragGestures { change, _ ->
                                colorPosY = (change.position.y).coerceIn(0f..shadeBoxSizeWithDensity)
                                color = getColor(color, change.position, shadeBoxSizeWithDensity.toInt())
                            }
                        }

                )

                Icon(
                    painter = painterResource("arrow_left.png"),
                    contentDescription = null,
                    tint = iconsColor,
                    modifier = Modifier
                        .offset { IntOffset(0, colorPosY.toInt() - 5.dp.roundToPx()) }
                        .size(10.dp)
                )

            }

        }

// Horizontal alpha rectangle
        Column {

            Icon(
                painter = painterResource("arrow_down.png"),
                contentDescription = null,
                tint = iconsColor,
                modifier = Modifier
                    .offset { IntOffset(alphaPosX.toInt() - 5.dp.roundToPx(), 0) }
                    .size(10.dp)
            )

            Box(Modifier.size(correctShadeBoxSize.dp, correctColorRectWidth.dp)) {

                Image(
                    painter = painterResource("chess_texture_gray.jpg"),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )

                Box(
                    Modifier
                        .size(correctShadeBoxSize.dp, correctColorRectWidth.dp)
                        .background(brush = Brush.horizontalGradient( listOf(Color.Transparent, colorShade) ) )
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    val position = it
                                    alphaPosX = position.x
                                    resultColor = colorShade.copy(alpha = getAlpha(
                                        position,
                                        shadeBoxSizeWithDensity.toInt()
                                    )
                                    )
                                }
                            )
                        }
                        .pointerInput(Unit) {
                            detectDragGestures { change, _ ->
                                alphaPosX = (change.position.x).coerceIn(0f..shadeBoxSizeWithDensity)
                                resultColor = colorShade.copy(alpha = getAlpha(
                                    change.position,
                                    shadeBoxSizeWithDensity.toInt()
                                )
                                )
                            }
                        }

                )

            }

        }

    }


}


// Color manipulation block

/** Get [Color] based on cursor [Offset] in color rectangle */
private fun getColor(initialColor: Color, offset: Offset, height: Int): Color {

    val offsetY = offset.y
    val part = height.toFloat() / 6
    val stage = (offsetY / part).toInt()

    var red = initialColor.red
    var green = initialColor.green
    var blue = initialColor.blue


    if (height > 0) {
        when(stage) {
            0 -> {
                red = 1f
                blue = offsetY / part
                green = 0f
            }
            1 -> {
                blue = 1f
                red = 1 - (offsetY % part) / part
                green = 0f
            }
            2 -> {
                blue = 1f
                green = (offsetY % part) / part
                red = 0f
            }
            3 -> {
                green = 1f
                blue = 1 - (offsetY % part) / part
                red = 0f
            }
            4 -> {
                green = 1f
                red = (offsetY % part) / part
                blue = 0f
            }
            5 -> {
                red = 1f
                green = 1 - (offsetY % part) / part
                blue = 0f
            }
            6 -> {
                green = 0f
            }
        }
    }

    red = red.coerceIn(0f..1f)
    green = green.coerceIn(0f..1f)
    blue = blue.coerceIn(0f..1f)

    return Color(red, green, blue)

}


/** Calculate color shade based on given [Color] & cursor [Offset] in color picker box */
private fun getColorShade(color: Color, offset: Offset, size: Int): Color {

    val x = (size - offset.x) / size
    val y = (size - offset.y) / size

    var red = color.red
    var green = color.green
    var blue = color.blue

    red = ( red + (1f - red) * x) * y
    green = ( green + (1f - green) * x) * y
    blue = ( blue + (1f - blue) * x) * y

    red = red.coerceIn(0f..1f)
    green = green.coerceIn(0f..1f)
    blue = blue.coerceIn(0f..1f)

    return Color(red, green, blue)

}


/** Get alpha based on cursor [Offset] in alpha rectangle */
private fun getAlpha(offset: Offset, width: Int): Float {
    return (offset.x / width.toFloat()).coerceIn(0f..1f)
}


/** Get correct shade box background color */
private fun getShadeBoxBackground(color: Color): Color {

    val red = color.red
    val green = color.green
    val blue = color.blue
    val sort = listOf(red, green, blue).sortedDescending()
    val offsetX = sort.last() / sort.first()

    val resultColor: Color = when {
        red > blue && blue > green -> Color(red = 1f, green = 0f, blue = (blue / red - offsetX) / (1 - offsetX))
        blue > red && red > green -> Color(red = (red / blue - offsetX) / (1 - offsetX), green = 0f, blue = 1f)
        blue > green && green > red -> Color(red = 0f, green = (green / blue - offsetX) / (1 - offsetX), blue = 1f)
        green > blue && blue > red -> Color(red = 0f, green = 1f, blue = (blue / green - offsetX) / (1 - offsetX))
        green > red && red > blue -> Color(red = (red / green - offsetX) / (1 - offsetX), green = 1f, blue = 0f)
        red > green && green > blue -> Color(red = 1f, green = (green / red - offsetX) / (1 - offsetX), blue = 0f)
        red > green && green == blue -> Color(red = 1f, green = 0f, blue = 0f)
        green > red && red == blue -> Color(red = 0f, green = 1f, blue = 0f)
        blue > red && red == green -> Color(red = 0f, green = 0f, blue = 1f)
        red == green && red > blue -> Color(red = 1f, green = 1f, blue = 0f)
        blue == green && blue > red -> Color(red = 0f, green = 1f, blue = 1f)
        red == blue && red > green -> Color(red = 1f, green = 0f, blue = 1f)
        else -> Color.Red
    }

    return resultColor

}


// Calculate cursor offset from 0.0 position to it center

/** Get center [Offset] of shade selector circle */
private fun getCenter(size: Int, density: Float): Offset {
    return Offset(
        x = (size.toFloat() * density) / 2,
        y = (size.toFloat() * density) / 2
    )
}


// Cursor position from given color block

/** Get color cursor offset based on given color */
private fun getColorCursorPosition(color: Color, height: Int): Float {

    val red = color.red
    val green = color.green
    val blue = color.blue
    val part = height.toFloat() / 6
    val sort = listOf(red, green, blue).sortedDescending()
    val offsetX = if (sort.first() != 0f) sort.last() / sort.first() else 0f
    val secondaryColorOffset = (sort[1] / sort.first() - offsetX) / (1 - offsetX)

    val stage = when {
        red > blue && blue == green -> 0
        red > blue && blue > green -> 0
        red == blue && red > green -> 1
        blue > red && red > green -> 1
        blue > red && red == green -> 2
        blue > green && green > red -> 2
        blue == green && green > red -> 3
        green > blue && blue > red -> 3
        green > blue && blue == red -> 4
        green > red && red > blue -> 4
        green == red && red > blue -> 5
        red > green && green > blue -> 5
        red > green && green == blue -> 6
        else -> 0
    }

    return if (red == green && green == blue) 0f else (stage * part) +
            if (stage == 1 || stage == 3 || stage == 5) {
                (1 - secondaryColorOffset) * part
            } else (secondaryColorOffset * part)

}


/** Get shade box cursor offset based on given color */
private fun getShadeCursorOffset(color: Color, size: Int): Offset {

    val sort = listOf(color.red, color.green, color.blue).sortedDescending()
    val offsetX = if (sort.first() != 0f) (1 - sort.last() / sort.first()) * size else 0f
    val offsetY = (1 - sort.first()) * size

    return Offset(offsetX, offsetY)

}


/** Get alpha cursor offset based on given color's alpha */
private fun getAlphaCursorPosition(color: Color, width: Int): Float {
    return width * color.alpha
}