package com.advice.products.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.advice.core.local.products.ProductSelection
import com.advice.core.utils.Storage
import com.advice.products.data.repositories.ProductsRepository
import com.advice.products.presentation.state.ProductsState
import com.advice.products.utils.toJson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ProductsViewModel : ViewModel(), KoinComponent {

    private val repository by inject<ProductsRepository>()
    private val storage by inject<Storage>()

    private val selections = mutableListOf<ProductSelection>()

    private val _state = MutableStateFlow(ProductsState.EMPTY)
    val state: Flow<ProductsState> = _state

    init {
        _state.value = _state.value.copy(showMerchInformation = !storage.hasSeenMerchInformation())

        viewModelScope.launch {
            repository.conference.collect {
                _state.value = _state.value.copy(canAdd = it.flags["enable_merch_cart"] ?: false, merchDocument = it.merchDocumentId)
            }
        }
        viewModelScope.launch {
            repository.products.collect {
                val shuffled = it.shuffled().filter { it.hasMedia() }
                val featured = shuffled.take(5)

                _state.value = _state.value.copy(featured = featured, products = it)

                updateList()
                updateSummary()
            }
        }
    }

    fun addToCart(selection: ProductSelection) {
        viewModelScope.launch {
            // Look to see if it already exists in our selection
            val indexOf =
                selections.indexOfFirst { it.id == selection.id && it.selectionOption == selection.selectionOption }
            if (indexOf != -1) {
                // add the two together
                selections[indexOf] =
                    selections[indexOf].copy(quantity = selections[indexOf].quantity + selection.quantity)
            } else {
                // add to the end of the list
                selections.add(selection)
            }

            updateList()
            updateSummary()
        }
    }

    private suspend fun updateList() {
        // merging the merch list with the selections
        val list = _state.value.products.map { model ->
            val quantity = selections.filter { it.id == model.id }.sumOf { it.quantity }
            model.copy(quantity = quantity)
        }

        _state.emit(_state.value.copy(products = list))
    }

    private suspend fun updateSummary() {
        // updating the summary broke down based on selections
        val summary = selections.map { selection ->
            val element = _state.value.products.find { it.id == selection.id }!!
            element.update(selection)
        }

        _state.emit(
            _state.value.copy(
                cart = summary,
                json = summary.toJson(),
            )
        )
    }

    fun setQuantity(id: Long, quantity: Int, selectedOption: String?) {
        viewModelScope.launch {
            val indexOf =
                selections.indexOfFirst { it.id == id && it.selectionOption == selectedOption }
            if (indexOf != -1) {
                val element = selections[indexOf]
                if (quantity == 0) {
                    selections.removeAt(indexOf)
                } else {
                    selections[indexOf] = element.copy(quantity = quantity)
                }
            }

            updateList()
            updateSummary()
        }
    }

    fun dismiss() {
        storage.dismissMerchInformation()
        _state.value = _state.value.copy(showMerchInformation = false)
    }
}
