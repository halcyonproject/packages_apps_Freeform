package com.libremobileos.sidebar.ui.all_app

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hlcyn.ui.components.rememberDrawablePainter
import com.libremobileos.sidebar.bean.AppInfo

@Composable
fun AllAppGridView(
    viewModel: AllAppViewModel,
    onClick: (AppInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    val appList by viewModel.appListFlow.collectAsState()
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 75.dp),
            modifier = modifier
        ) {
            items(appList) { appInfo ->
                AllAppGridItem(
                    appInfo = appInfo,
                    onClick = onClick
                )
            }
        }
    }
}

@Composable
fun AllAppGridItem(
    appInfo: AppInfo,
    onClick: (AppInfo) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .padding(8.dp)
            .clickable { onClick(appInfo) }
    ) {
        Image(
            painter = rememberDrawablePainter(appInfo.icon),
            contentDescription = appInfo.label,
            modifier = Modifier.size(50.dp)
        )
        Text(
            text = appInfo.label,
            maxLines = 1,
            fontSize = 12.sp,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
