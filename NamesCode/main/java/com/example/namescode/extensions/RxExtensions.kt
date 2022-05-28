package com.example.namescode.extensions

import io.reactivex.rxjava3.disposables.Disposable

fun Disposable?.disposeIfNeed() {
    if (this?.isDisposed?.not() == true) {
        this.dispose()
    }
}
