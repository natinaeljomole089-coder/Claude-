package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SentimentPoint

@Composable
fun SentimentChart(
    timeline: List<SentimentPoint>,
    modifier: Modifier = Modifier
) {
    if (timeline.isEmpty()) return

    // State to hold the currently selected/tapped data point index
    var selectedIndex by remember { mutableStateOf(-1) }

    val salespersonColor = Color(0xFF10B981) // Emerald Green
    val prospectColor = Color(0xFF3B82F6) // Bright Blue
    val overallSentimentColor = Color(0xFFF59E0B) // Amber/Yellow
    val gridColor = Color.LightGray.copy(alpha = 0.3f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, shape = RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        // Chart Header with Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Call Engagement & Sentiment Graph",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LegendItem(label = "Salesperson", color = salespersonColor)
                LegendItem(label = "Prospect", color = prospectColor)
                LegendItem(label = "Overall", color = overallSentimentColor)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(timeline) {
                        detectTapGestures { offset ->
                            val width = size.width
                            val stepX = width / (timeline.size - 1).coerceAtLeast(1)
                            val index = (offset.x / stepX + 0.5f).toInt()
                            selectedIndex = if (index in timeline.indices) index else -1
                        }
                    }
            ) {
                val width = size.width
                val height = size.height

                val paddingBottom = 20.dp.toPx()
                val chartHeight = height - paddingBottom
                val stepX = width / (timeline.size - 1).coerceAtLeast(1)

                // 1. Draw Grid Lines & Labels
                val gridLines = 4
                for (i in 0..gridLines) {
                    val y = chartHeight * (i.toFloat() / gridLines)
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // 2. Plot lines
                val salespersonPath = Path()
                val prospectPath = Path()
                val sentimentPath = Path()

                timeline.forEachIndexed { index, point ->
                    val x = index * stepX
                    // Reverse the Y axis because canvas starts at top-left
                    // Normalize value from 0-100
                    val ySales = chartHeight * (1f - (point.salespersonEngagement / 100f).coerceIn(0f, 1f))
                    val yProspect = chartHeight * (1f - (point.prospectEngagement / 100f).coerceIn(0f, 1f))
                    val ySentiment = chartHeight * (1f - (point.sentiment / 100f).coerceIn(0f, 1f))

                    if (index == 0) {
                        salespersonPath.moveTo(x, ySales)
                        prospectPath.moveTo(x, yProspect)
                        sentimentPath.moveTo(x, ySentiment)
                    } else {
                        salespersonPath.lineTo(x, ySales)
                        prospectPath.lineTo(x, yProspect)
                        sentimentPath.lineTo(x, ySentiment)
                    }
                }

                // Draw Paths
                drawPath(
                    path = salespersonPath,
                    color = salespersonColor,
                    style = Stroke(width = 3.dp.toPx())
                )
                drawPath(
                    path = prospectPath,
                    color = prospectColor,
                    style = Stroke(width = 3.dp.toPx())
                )
                drawPath(
                    path = sentimentPath,
                    color = overallSentimentColor,
                    style = Stroke(width = 2.dp.toPx())
                )

                // 3. Draw Points & Interactive Tooltip Line
                timeline.forEachIndexed { index, point ->
                    val x = index * stepX
                    val ySales = chartHeight * (1f - (point.salespersonEngagement / 100f).coerceIn(0f, 1f))
                    val yProspect = chartHeight * (1f - (point.prospectEngagement / 100f).coerceIn(0f, 1f))
                    val ySentiment = chartHeight * (1f - (point.sentiment / 100f).coerceIn(0f, 1f))

                    // Draw little dots at actual timestamps
                    drawCircle(color = salespersonColor, radius = 4.dp.toPx(), center = Offset(x, ySales))
                    drawCircle(color = prospectColor, radius = 4.dp.toPx(), center = Offset(x, yProspect))
                    drawCircle(color = overallSentimentColor, radius = 4.dp.toPx(), center = Offset(x, ySentiment))
                }

                // Draw Vertical Guide Line and text if selected
                if (selectedIndex in timeline.indices) {
                    val x = selectedIndex * stepX
                    drawLine(
                        color = Color.DarkGray.copy(alpha = 0.5f),
                        start = Offset(x, 0f),
                        end = Offset(x, chartHeight),
                        strokeWidth = 1.5.dp.toPx()
                    )

                    val point = timeline[selectedIndex]
                    val ySales = chartHeight * (1f - (point.salespersonEngagement / 100f).coerceIn(0f, 1f))
                    val yProspect = chartHeight * (1f - (point.prospectEngagement / 100f).coerceIn(0f, 1f))
                    val ySentiment = chartHeight * (1f - (point.sentiment / 100f).coerceIn(0f, 1f))

                    drawCircle(color = Color.Black, radius = 6.dp.toPx(), center = Offset(x, ySales))
                    drawCircle(color = Color.Black, radius = 6.dp.toPx(), center = Offset(x, yProspect))
                    drawCircle(color = Color.Black, radius = 6.dp.toPx(), center = Offset(x, ySentiment))
                }
            }
        }

        // Timeline text markers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            timeline.forEachIndexed { index, point ->
                // Draw up to 5 timeline markers to prevent clutter
                if (timeline.size <= 5 || index % (timeline.size / 4).coerceAtLeast(1) == 0 || index == timeline.lastIndex) {
                    Text(
                        text = point.time,
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(40.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }
            }
        }

        // Interactive Details Box
        if (selectedIndex in timeline.indices) {
            val point = timeline[selectedIndex]
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.LightGray.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Time: ${point.time}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "Salesperson: ${point.salespersonEngagement.toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = salespersonColor
                        )
                        Text(
                            text = "Prospect: ${point.prospectEngagement.toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = prospectColor
                        )
                        Text(
                            text = "Sentiment: ${point.sentiment.toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = overallSentimentColor
                        )
                    }
                }
            }
        } else {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap on the chart to inspect details at a specific timestamp",
                fontSize = 11.sp,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, shape = RoundedCornerShape(2.dp))
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color.DarkGray
        )
    }
}
