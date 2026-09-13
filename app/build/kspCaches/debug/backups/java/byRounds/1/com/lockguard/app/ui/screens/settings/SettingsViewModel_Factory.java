package com.lockguard.app.ui.screens.settings;

import com.lockguard.app.data.repository.IntruderRepository;
import com.lockguard.app.data.repository.SettingsRepository;
import com.lockguard.app.data.security.PinManager;
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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<SettingsRepository> settingsRepositoryProvider;

  private final Provider<IntruderRepository> intruderRepositoryProvider;

  private final Provider<PinManager> pinManagerProvider;

  public SettingsViewModel_Factory(Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<IntruderRepository> intruderRepositoryProvider,
      Provider<PinManager> pinManagerProvider) {
    this.settingsRepositoryProvider = settingsRepositoryProvider;
    this.intruderRepositoryProvider = intruderRepositoryProvider;
    this.pinManagerProvider = pinManagerProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(settingsRepositoryProvider.get(), intruderRepositoryProvider.get(), pinManagerProvider.get());
  }

  public static SettingsViewModel_Factory create(
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<IntruderRepository> intruderRepositoryProvider,
      Provider<PinManager> pinManagerProvider) {
    return new SettingsViewModel_Factory(settingsRepositoryProvider, intruderRepositoryProvider, pinManagerProvider);
  }

  public static SettingsViewModel newInstance(SettingsRepository settingsRepository,
      IntruderRepository intruderRepository, PinManager pinManager) {
    return new SettingsViewModel(settingsRepository, intruderRepository, pinManager);
  }
}
