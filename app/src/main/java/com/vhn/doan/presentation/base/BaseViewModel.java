package com.vhn.doan.presentation.base;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.vhn.doan.data.local.AppDatabase;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * BaseViewModel - Base class cho tất cả ViewModels
 * Kế thừa AndroidViewModel để có access vào Application context
 * Quản lý lifecycle và cleanup resources
 */
public abstract class BaseViewModel extends AndroidViewModel {

    protected final AppDatabase database;
    protected final CompositeDisposable compositeDisposable;

    // LiveData cho loading state
    protected final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    // LiveData cho error messages
    protected final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // LiveData cho success messages
    protected final MutableLiveData<String> successMessage = new MutableLiveData<>();

    public BaseViewModel(@NonNull Application application) {
        super(application);
        this.database = AppDatabase.getInstance(application);
        this.compositeDisposable = new CompositeDisposable();
    }

    /**
     * Thêm Disposable vào CompositeDisposable để auto cleanup
     */
    protected void addDisposable(Disposable disposable) {
        compositeDisposable.add(disposable);
    }

    /**
     * Show loading state
     */
    protected void showLoading() {
        isLoading.postValue(true);
    }

    /**
     * Hide loading state
     */
    protected void hideLoading() {
        isLoading.postValue(false);
    }

    /**
     * Show error message
     */
    protected void showError(String message) {
        errorMessage.postValue(message);
        hideLoading();
    }

    /**
     * Show success message
     */
    protected void showSuccess(String message) {
        successMessage.postValue(message);
        hideLoading();
    }

    /**
     * Getters cho LiveData
     */
    public MutableLiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public MutableLiveData<String> getSuccessMessage() {
        return successMessage;
    }

    /**
     * Cleanup khi ViewModel bị destroy
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        // Dispose tất cả RxJava subscriptions
        if (!compositeDisposable.isDisposed()) {
            compositeDisposable.clear();
        }
    }
}
