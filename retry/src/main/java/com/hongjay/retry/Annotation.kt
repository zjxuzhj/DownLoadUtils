package com.hongjay.retry

import androidx.annotation.Keep

/**
 * 上传的类名
 */
@Keep
@Target(AnnotationTarget.CLASS)
annotation class UploadClass

/**
 * 上传对应的Bean
 */
@Keep
@Target(AnnotationTarget.FUNCTION)
annotation class ClassBean(val BeanClass: String)

/**
 * 用于添加ClassBean重复的方法
 */
@Keep
@Target(AnnotationTarget.FUNCTION)
annotation class Repetition