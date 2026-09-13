package com.lockguard.app.ui.screens.applock;

import androidx.lifecycle.SavedStateHandle;
import com.lockguard.app.data.repository.IntruderRepository;
import com.lockguard.app.data.security.BiometricHelper;
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
public final class AppLockViewModel_Factory implements Factory<AppLockViewModel> {
  private final Provider<PinManager> pinManagerProvider;

  private final Provider<BiometricHelper> biometricHelperProvider;

  private final Provider<IntruderRepository> intruderRepositoryProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  public AppLockViewModel_Factory(Provider<PinManager> pinManagerProvider,
      Provider<BiometricHelper> biometricHelperProvider,
      Provider<IntruderRepository> intruderRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.pinManagerProvider = pinManagerProvider;
    this.biometricHelperProvider = biometricHelperProvider;
    this.intruderRepositoryProvider = intruderRepositoryProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public AppLockViewModel get() {
    return newInstance(pinManagerProvider.get(), biometricHelperProvider.get(), intruderRepositoryProvider.get(), savedStateHandleProvider.get());
  }

  public static AppLockViewModel_Factory create(Provider<PinManager> pinManagerProvider,
      Provider<BiometricHelper> biometricHelperProvider,
      Provider<IntruderRepository> intruderRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new AppLockViewModel_Factory(pinManagerProvider, biometricHelperProvider, intruderRepositoryProvider, savedStateHandleProvider);
  }

  public static AppLockViewModel newInstance(PinManager pinManager, BiometricHelper biometricHelper,
      IntruderRepository intruderRepository, SavedStateHandle savedStateHandle) {
    return new AppLockViewModel(pinManager, biometricHelper, intruderRepository, savedStateHandle);
  }
}
