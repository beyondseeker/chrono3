package com.objectfanatics.chrono3

import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.Test

/**
 * RxJava の subscribe での例外ハンドリングが行われない例とそれをキャッチする例。
 * https://github.com/ReactiveX/RxJava/wiki/What's-different-in-2.0#error-handling
 */
class ExampleUnitTest {
    /**
     * subscribe() による例外ハンドリングをすり抜ける例
     */
    @Test
    fun example1() {
        val disposable = CompositeDisposable()
        disposable.add(
            Completable
                .fromCallable {
                    println("callable: begin")

                    // この sleep 中に disposable.dispose() が呼ばれ、このスレッドが interrupt され java.lang.InterruptedException
                    // がスローされるが、既に dispose されているので subscribe() にてこの例外をハンドリングできない。
                    Thread.sleep(200)

                    println("callable: end")
                }
                .subscribeOn(Schedulers.newThread())
                .subscribe({ onSuccess(disposable) }, ::onError)
        )

        Thread.sleep(100)

        disposable.dispose()
        println("disposed!")

        Thread.sleep(300)
    }

    /**
     * subscribe() によるハンドリングをすり抜た例外を catch する例
     */
    @Test
    fun example2() {
        RxJavaPlugins.setErrorHandler { throwable -> println("RxJava Global Error: ${throwable.message}") }
        example1()
    }

    private fun onSuccess(disposable: Disposable) {
        println("onSuccess: isDisposed = ${disposable.isDisposed}")
    }

    private fun onError(error: Throwable) {
        println("onError: ${error.message}")
    }
}