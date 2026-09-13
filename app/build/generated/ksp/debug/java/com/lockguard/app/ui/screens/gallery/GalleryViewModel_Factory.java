package com.lockguard.app.ui.screens.gallery;

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
public final class GalleryViewModel_Factory implements Factory<GalleryViewModel> {
  private final Provider<IntruderRepository> intruderRepositoryProvider;

  public GalleryViewModel_Factory(Provider<IntruderRepository> intruderRepositoryProvider) {
    this.intruderRepositoryProvider = intruderRepositoryProvider;
  }

  @Override
  public GalleryViewModel get() {
    return newInstance(intruderRepositoryProvider.get());
  }

  public static GalleryViewModel_Factory create(
      Provider<IntruderRepository> intruderRepositoryProvider) {
    return new GalleryViewModel_Factory(intruderRepositoryProvider);
  }

  public static GalleryViewModel newInstance(IntruderRepository intruderRepository) {
    return new GalleryViewModel(intruderRepository);
  }
}
