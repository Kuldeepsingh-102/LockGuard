package com.lockguard.app;

import com.lockguard.app.service.NotificationHelper;
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
public final class LockGuardApp_MembersInjector implements MembersInjector<LockGuardApp> {
  private final Provider<NotificationHelper> notificationHelperProvider;

  public LockGuardApp_MembersInjector(Provider<NotificationHelper> notificationHelperProvider) {
    this.notificationHelperProvider = notificationHelperProvider;
  }

  public static MembersInjector<LockGuardApp> create(
      Provider<NotificationHelper> notificationHelperProvider) {
    return new LockGuardApp_MembersInjector(notificationHelperProvider);
  }

  @Override
  public void injectMembers(LockGuardApp instance) {
    injectNotificationHelper(instance, notificationHelperProvider.get());
  }

  @InjectedFieldSignature("com.lockguard.app.LockGuardApp.notificationHelper")
  public static void injectNotificationHelper(LockGuardApp instance,
      NotificationHelper notificationHelper) {
    instance.notificationHelper = notificationHelper;
  }
}
