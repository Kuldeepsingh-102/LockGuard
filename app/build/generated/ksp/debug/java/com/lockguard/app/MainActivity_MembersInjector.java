package com.lockguard.app;

import com.lockguard.app.data.repository.SettingsRepository;
import com.lockguard.app.data.security.PinManager;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class MainActivity_MembersInjector implements MembersInjector<MainActivity> {
  private final Provider<PinManager> pinManagerProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  public MainActivity_MembersInjector(Provider<PinManager> pinManagerProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    this.pinManagerProvider = pinManagerProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  public static MembersInjector<MainActivity> create(Provider<PinManager> pinManagerProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new MainActivity_MembersInjector(pinManagerProvider, settingsRepositoryProvider);
  }

  @Override
  public void injectMembers(MainActivity instance) {
    injectPinManager(instance, pinManagerProvider.get());
    injectSettingsRepository(instance, settingsRepositoryProvider.get());
  }

  @InjectedFieldSignature("com.lockguard.app.MainActivity.pinManager")
  public static void injectPinManager(MainActivity instance, PinManager pinManager) {
    instance.pinManager = pinManager;
  }

  @InjectedFieldSignature("com.lockguard.app.MainActivity.settingsRepository")
  public static void injectSettingsRepository(MainActivity instance,
      SettingsRepository settingsRepository) {
    instance.settingsRepository = settingsRepository;
  }
}
