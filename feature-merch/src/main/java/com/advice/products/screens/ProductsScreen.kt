package com.advice.products.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.advice.core.local.Product
import com.advice.core.ui.ProductsState
import com.advice.products.views.ProductView
import com.advice.ui.preview.LightDarkPreview
import com.advice.ui.preview.ProductsProvider
import com.advice.ui.theme.ScheduleTheme
import com.advice.products.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    state: ProductsState,
    onSummaryClicked: () -> Unit, onProductClicked: (Product) -> Unit,
) {
    val list = state.elements

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Merch") },
                actions = {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(painterResource(id = R.drawable.ic_filter), null)
                    }
                },
            )
        },
        floatingActionButton = {
            val itemCount = state.elements.sumOf { it.quantity }
            if (itemCount > 0) {
                FloatingActionButton(
                    onClick = onSummaryClicked,
                    Modifier
                        .padding(horizontal = 32.dp)
                        .fillMaxWidth(),
                    shape = FloatingActionButtonDefaults.extendedFabShape
                ) {
                    Text("View Cart ($itemCount)")
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) {
        ProductsScreenContent(list, onProductClicked, Modifier.padding(it))
    }
}

@Composable
fun ProductsScreenContent(
    list: List<Product>,
    onProductClicked: (Product) -> Unit,
    modifier: Modifier,
) {
    LazyColumn(modifier, contentPadding = PaddingValues(vertical = 16.dp)) {
        items(list) {
            ProductView(it, onProductClicked)
        }
        item {
            Spacer(Modifier.height(64.dp))
        }
    }
}

@LightDarkPreview
@Composable
fun ProductsScreenPreview(@PreviewParameter(ProductsProvider::class) state: ProductsState) {
    ScheduleTheme {
        ProductsScreen(state, {}, {})
    }
}