package com.lockguard.app.di;

import android.content.Context;
import com.lockguard.app.data.database.LockGuardDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class DatabaseModule_ProvideLockGuardDatabaseFactory implements Factory<LockGuardDatabase> {
  private final Provider<Context> contextProvider;

  public DatabaseModule_ProvideLockGuardDatabaseFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public LockGuardDatabase get() {
    return provideLockGuardDatabase(contextProvider.get());
  }

  public static DatabaseModule_ProvideLockGuardDatabaseFactory create(
      Provider<Context> contextProvider) {
    return new DatabaseModule_ProvideLockGuardDatabaseFactory(contextProvider);
  }

  public static LockGuardDatabase provideLockGuardDatabase(Context context) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideLockGuardDatabase(context));
  }
}
