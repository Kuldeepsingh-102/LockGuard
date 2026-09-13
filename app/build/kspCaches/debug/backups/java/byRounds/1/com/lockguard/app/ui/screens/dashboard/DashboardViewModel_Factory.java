package com.lockguard.app.ui.screens.dashboard;

import com.lockguard.app.data.repository.IntruderRepository;
import com.lockguard.app.data.repository.SettingsRepository;
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
public final class DashboardViewModel_Factory implements Factory<DashboardViewModel> {
  private final Provider<IntruderRepository> intruderRepositoryProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  public DashboardViewModel_Factory(Provider<IntruderRepository> intruderRepositoryProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    this.intruderRepositoryProvider = intruderRepositoryProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  @Override
  public DashboardViewModel get() {
    return newInstance(intruderRepositoryProvider.get(), settingsRepositoryProvider.get());
  }

  public static DashboardViewModel_Factory create(
      Provider<IntruderRepository> intruderRepositoryProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new DashboardViewModel_Factory(intruderRepositoryProvider, settingsRepositoryProvider);
  }

  public static DashboardViewModel newInstance(IntruderRepository intruderRepository,
      SettingsRepository settingsRepository) {
    return new DashboardViewModel(intruderRepository, settingsRepository);
  }
}
