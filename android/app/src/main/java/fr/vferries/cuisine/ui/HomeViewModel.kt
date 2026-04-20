package fr.vferries.cuisine.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.vferries.cuisine.data.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: RecipeRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<HomeState>(HomeState.Loading)
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.value = try {
                HomeState.Success(repository.listRecipes())
            } catch (e: Exception) {
                HomeState.Error(e.message ?: "Erreur inconnue")
            }
        }
    }
}
