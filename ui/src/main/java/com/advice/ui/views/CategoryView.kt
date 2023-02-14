package com.advice.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.advice.schedule.models.firebase.FirebaseTag

@Composable
fun CategoryView(tag: FirebaseTag) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)) {
        Box(
            Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(Color(android.graphics.Color.parseColor(tag.color)))
        )
        Spacer(Modifier.width(4.dp))
        Text(tag.label, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Preview(showBackground = true)
@Composable
fun CategoryViewPreview() {
    MaterialTheme {
        CategoryView(FirebaseTag(label = "Talk", color_background = "#EE11EE"))
    }
}