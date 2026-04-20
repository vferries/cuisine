package fr.vferries.cuisine.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.vferries.cuisine.data.Recipe
import fr.vferries.cuisine.data.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface RecipeState {
    data object Loading : RecipeState
    data class Success(val recipe: Recipe) : RecipeState
    data class Error(val message: String) : RecipeState
}

class RecipeViewModel(
    private val repository: RecipeRepository,
    private val slug: String,
) : ViewModel() {

    private val _state = MutableStateFlow<RecipeState>(RecipeState.Loading)
    val state: StateFlow<RecipeState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.value = try {
                RecipeState.Success(repository.getRecipe(slug))
            } catch (e: Exception) {
                RecipeState.Error(e.message ?: "Erreur inconnue")
            }
        }
    }
}
