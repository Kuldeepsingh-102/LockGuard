package com.lockguard.app.ui.screens.detail;

import androidx.lifecycle.SavedStateHandle;
import com.lockguard.app.data.repository.IntruderRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class PhotoDetailViewModel_Factory implements Factory<PhotoDetailViewModel> {
  private final Provider<IntruderRepository> intruderRepositoryProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  public PhotoDetailViewModel_Factory(Provider<IntruderRepository> intruderRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.intruderRepositoryProvider = intruderRepositoryProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public PhotoDetailViewModel get() {
    return newInstance(intruderRepositoryProvider.get(), savedStateHandleProvider.get());
  }

  public static PhotoDetailViewModel_Factory create(
      Provider<IntruderRepository> intruderRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new PhotoDetailViewModel_Factory(intruderRepositoryProvider, savedStateHandleProvider);
  }

  public static PhotoDetailViewModel newInstance(IntruderRepository intruderRepository,
      SavedStateHandle savedStateHandle) {
    return new PhotoDetailViewModel(intruderRepository, savedStateHandle);
  }
}
