// Copy kotlin-dsl extensions because I can't find them published anywhere except gradle-public-api, which I can't import in Amper

/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.kotlin.dsl

import org.gradle.api.DomainObjectCollection
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectCollection
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.PolymorphicDomainObjectContainer
import org.gradle.api.Task
import org.gradle.api.UnknownDomainObjectException
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.file.FileSystemLocationProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.api.provider.HasMultipleValues
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.TaskCollection
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import java.io.File
import kotlin.reflect.KClass

import kotlin.reflect.KProperty
import kotlin.reflect.safeCast

inline fun <reified  T> typeOf() = T::class.java

/**
 * Looks for the extension of a given name. If none found it will throw an exception.
 *
 * @param name extension name
 * @return extension
 * @throws [UnknownDomainObjectException] When the given extension is not found.
 *
 * @see [ExtensionContainer.getByName]
 */
operator fun ExtensionContainer.get(name: String): Any =
    getByName(name)


/**
 * Looks for the extension of a given name and casts it to the expected type [T].
 *
 * If none found it will throw an [UnknownDomainObjectException].
 * If the extension is found but cannot be cast to the expected type it will throw an [IllegalStateException].
 *
 * @param name extension name
 * @return extension, never null
 * @throws [UnknownDomainObjectException] When the given extension is not found.
 * @throws [IllegalStateException] When the given extension cannot be cast to the expected type.
 */
@Suppress("extension_shadowed_by_member")
inline fun <reified T : Any> ExtensionContainer.getByName(name: String) =
    getByName(name).let {
        it as? T
            ?: error(
                "Element '$name' of type '${it::class.java.name}' from container '$this' cannot be cast to '${T::class.qualifiedName}'."
            )
    }


/**
 * Delegated property getter that locates extensions.
 */
inline operator fun <reified T : Any> ExtensionContainer.getValue(thisRef: Any?, property: KProperty<*>): T =
    getByName<T>(property.name)


/**
 * Adds a new extension to this container.
 *
 * @param T the public type of the added extension
 * @param name the name of the extension
 * @param extension the extension instance
 *
 * @throws IllegalArgumentException When an extension with the given name already exists.
 *
 * @see [ExtensionContainer.add]
 * @since 5.0
 */
@Suppress("extension_shadowed_by_member")
inline fun <reified T : Any> ExtensionContainer.add(name: String, extension: T) {
    add(typeOf<T>(), name, extension)
}


/**
 * Creates and adds a new extension to this container.
 *
 * @param T the instance type of the new extension
 * @param name the extension's name
 * @param constructionArguments construction arguments
 * @return the created instance
 *
 * @see [ExtensionContainer.create]
 * @since 5.0
 */
inline fun <reified T : Any> ExtensionContainer.create(name: String, vararg constructionArguments: Any): T =
    create(name, T::class.java, *constructionArguments)


/**
 * Looks for the extension of a given type.
 *
 * @param T the extension type
 * @return the extension
 * @throws UnknownDomainObjectException when no matching extension can be found
 *
 * @see [ExtensionContainer.getByType]
 * @since 5.0
 */
inline fun <reified T : Any> ExtensionContainer.getByType(): T =
    getByType(typeOf<T>())


/**
 * Looks for the extension of a given type.
 *
 * @param T the extension type
 * @return the extension or null if not found
 *
 * @see [ExtensionContainer.findByType]
 * @since 5.0
 */
inline fun <reified T : Any> ExtensionContainer.findByType(): T? =
    findByType(typeOf<T>())


/**
 * Looks for the extension of the specified type and configures it with the supplied action.
 *
 * @param T the extension type
 * @param action the configuration action
 *
 * @see [ExtensionContainer.configure]
 * @since 5.0
 */
inline fun <reified T : Any> ExtensionContainer.configure(noinline action: T.() -> Unit) {
    configure(typeOf<T>(), action)
}



/**
 * Returns a collection containing the objects in this collection of the given type. The
 * returned collection is live, so that when matching objects are later added to this
 * collection, they are also visible in the filtered collection.
 *
 * @param S The type of objects to find.
 * @return The matching objects. Returns an empty collection if there are no such objects
 * in this collection.
 * @see [NamedDomainObjectCollection.withType]
 */
inline fun <reified S : Any> NamedDomainObjectCollection<in S>.withType(): NamedDomainObjectCollection<S> =
    withType(S::class.java)


/**
 * Idiomatic way of referring to the provider of a well-known element of a collection via a delegate property.
 *
 * `tasks { val jar by existing }`
 *
 * @param T the domain object type
 * @param C the concrete container type
 */
inline val <T : Any, C : NamedDomainObjectCollection<T>> C.existing: ExistingDomainObjectDelegateProvider<out C>
    get() = ExistingDomainObjectDelegateProvider.of(this)


/**
 * Idiomatic way of referring to the provider of a well-known element of a collection via a delegate property.
 *
 * `tasks { val jar by existing { ... } }`
 *
 * @param T the domain object type
 * @param C the concrete container type
 * @param action the configuration action
 */
fun <T : Any, C : NamedDomainObjectCollection<T>> C.existing(action: T.() -> Unit): ExistingDomainObjectDelegateProviderWithAction<out C, T> =
    ExistingDomainObjectDelegateProviderWithAction.of(this, action)


/**
 * Idiomatic way of referring to the provider of a well-known element of a collection via a delegate property.
 *
 * `tasks { val jar by existing(Jar::class) }`
 *
 * @param T the domain object type
 * @param C the concrete container type
 * @param type the domain object type
 */
fun <T : Any, C : NamedDomainObjectCollection<T>, U : T> C.existing(type: KClass<U>): ExistingDomainObjectDelegateProviderWithType<out C, U> =
    ExistingDomainObjectDelegateProviderWithType.of(this, type)


/**
 * Idiomatic way of referring to the provider of a well-known element of a collection via a delegate property.
 *
 * `tasks { val jar by existing(Jar::class) { ... } }`
 *
 * @param T the domain object type
 * @param C the concrete container type
 * @param type the domain object type
 * @param action the configuration action
 */
fun <T : Any, C : NamedDomainObjectCollection<T>, U : T> C.existing(type: KClass<U>, action: U.() -> Unit): ExistingDomainObjectDelegateProviderWithTypeAndAction<out C, U> =
    ExistingDomainObjectDelegateProviderWithTypeAndAction.of(this, type, action)


/**
 * Holds the delegate provider for the `existing` property delegate with
 * the purpose of providing specialized implementations for the `provideDelegate` operator
 * based on the static type of the provider.
 */
class ExistingDomainObjectDelegateProvider<T>
private constructor(
    internal val delegateProvider: T
) {
    companion object {
        fun <T> of(delegateProvider: T) =
            ExistingDomainObjectDelegateProvider(delegateProvider)
    }
}


/**
 * Holds the delegate provider for the `existing` property delegate with
 * the purpose of providing specialized implementations for the `provideDelegate` operator
 * based on the static type of the provider.
 */
class ExistingDomainObjectDelegateProviderWithAction<C, T>
private constructor(
    internal val delegateProvider: C,
    internal val action: T.() -> Unit
) {
    companion object {
        fun <C, T> of(delegateProvider: C, action: T.() -> Unit) =
            ExistingDomainObjectDelegateProviderWithAction(delegateProvider, action)
    }
}


/**
 * Holds the delegate provider and expected element type for the `existing` property delegate with
 * the purpose of providing specialized implementations for the `provideDelegate` operator
 * based on the static type of the provider.
 */
class ExistingDomainObjectDelegateProviderWithType<T, U : Any>
private constructor(
    internal val delegateProvider: T,
    internal val type: KClass<U>
) {
    companion object {
        fun <T, U : Any> of(delegateProvider: T, type: KClass<U>) =
            ExistingDomainObjectDelegateProviderWithType(delegateProvider, type)
    }
}


/**
 * Holds the delegate provider and expected element type for the `existing` property delegate with
 * the purpose of providing specialized implementations for the `provideDelegate` operator
 * based on the static type of the provider.
 */
class ExistingDomainObjectDelegateProviderWithTypeAndAction<T, U : Any>
private constructor(
    internal val delegateProvider: T,
    internal val type: KClass<U>,
    internal val action: U.() -> Unit
) {
    companion object {
        fun <T, U : Any> of(delegateProvider: T, type: KClass<U>, action: U.() -> Unit) =
            ExistingDomainObjectDelegateProviderWithTypeAndAction(delegateProvider, type, action)
    }
}


/**
 * Provides access to the [NamedDomainObjectProvider] for the element of the given
 * property name from the container via a delegated property.
 */
operator fun <T : Any, C : NamedDomainObjectCollection<T>> ExistingDomainObjectDelegateProvider<C>.provideDelegate(
    receiver: Any?,
    property: KProperty<*>
) = ExistingDomainObjectDelegate.of(
    delegateProvider.named(property.name)
)


/**
 * Provides access to the [NamedDomainObjectProvider] for the element of the given
 * property name from the container via a delegated property.
 */
operator fun <T : Any, C : NamedDomainObjectCollection<T>> ExistingDomainObjectDelegateProviderWithAction<C, T>.provideDelegate(
    receiver: Any?,
    property: KProperty<*>
) = ExistingDomainObjectDelegate.of(
    delegateProvider.named(property.name).apply { configure(action) }
)


/**
 * Provides access to the [NamedDomainObjectProvider] for the element of the given
 * property name from the container via a delegated property.
 */
operator fun <T : Any, C : NamedDomainObjectCollection<T>, U : T> ExistingDomainObjectDelegateProviderWithType<C, U>.provideDelegate(
    receiver: Any?,
    property: KProperty<*>
) = ExistingDomainObjectDelegate.of(
    delegateProvider.named(property.name, type)
)


/**
 * Provides access to the [NamedDomainObjectProvider] for the element of the given
 * property name from the container via a delegated property.
 */
operator fun <T : Any, C : NamedDomainObjectCollection<T>, U : T> ExistingDomainObjectDelegateProviderWithTypeAndAction<C, U>.provideDelegate(
    receiver: Any?,
    property: KProperty<*>
) = ExistingDomainObjectDelegate.of(
    delegateProvider.named(property.name, type, action)
)


/**
 * Holds a property delegate with the purpose of providing specialized implementations for the
 * `getValue` operator based on the static type of the delegate.
 */
class ExistingDomainObjectDelegate<T>
private constructor(
    internal val delegate: T
) {
    companion object {
        fun <T> of(delegate: T) =
            ExistingDomainObjectDelegate(delegate)
    }
}


/**
 * Gets the delegate value.
 */
operator fun <T> ExistingDomainObjectDelegate<out T>.getValue(receiver: Any?, property: KProperty<*>): T =
    delegate


/**
 * Locates an object by name and type, without triggering its creation or configuration, failing if there is no such object.
 *
 * @see [NamedDomainObjectCollection.named]
 */
@Suppress("extension_shadowed_by_member")
inline fun <reified T : Any> NamedDomainObjectCollection<out Any>.named(name: String): NamedDomainObjectProvider<T> =
    named(name, T::class)


/**
 * Locates an object by name and type, without triggering its creation or configuration, failing if there is no such object.
 *
 * @see [NamedDomainObjectCollection.named]
 */
@Suppress("unchecked_cast")
fun <T : Any> NamedDomainObjectCollection<out Any>.named(name: String, type: KClass<T>): NamedDomainObjectProvider<T> =
    (this as NamedDomainObjectCollection<T>).named(name, type.java)


/**
 * Configures an object by name and type, without triggering its creation or configuration, failing if there is no such object.
 *
 * @see [NamedDomainObjectCollection.named]
 * @see [NamedDomainObjectProvider.configure]
 */
@Suppress("unchecked_cast")
inline fun <reified T : Any> NamedDomainObjectCollection<out Any>.named(name: String, noinline configuration: T.() -> Unit): NamedDomainObjectProvider<T> =
    (this as NamedDomainObjectCollection<T>).named(name, T::class.java, configuration)


/**
 * Configures an object by name and type, without triggering its creation or configuration, failing if there is no such object.
 *
 * @see [NamedDomainObjectCollection.named]
 * @see [NamedDomainObjectProvider.configure]
 */
@Suppress("unchecked_cast")
fun <T : Any> NamedDomainObjectCollection<out Any>.named(name: String, type: KClass<T>, configuration: T.() -> Unit): NamedDomainObjectProvider<T> =
    (this as NamedDomainObjectCollection<T>).named(name, type.java, configuration)


/**
 * Locates an object by name and casts it to the expected type [T].
 *
 * If an object with the given [name] is not found, [UnknownDomainObjectException] is thrown.
 * If the object is found but cannot be cast to the expected type [T], [IllegalArgumentException] is thrown.
 *
 * @param name object name
 * @return the object, never null
 * @throws [UnknownDomainObjectException] When the given object is not found.
 * @throws [IllegalArgumentException] When the given object cannot be cast to the expected type.
 */
@Suppress("extension_shadowed_by_member")
inline fun <reified T : Any> NamedDomainObjectCollection<out Any>.getByName(name: String) =
    getByName(name).let {
        it as? T
            ?: error("")//throw illegalElementType(this, name, T::class, it::class)
    }


/**
 * Locates an object by name and casts it to the expected [type].
 *
 * If an object with the given [name] is not found, [UnknownDomainObjectException] is thrown.
 * If the object is found but cannot be cast to the expected [type], [IllegalArgumentException] is thrown.
 *
 * @param name object name
 * @param type expected type
 * @return the object, never null
 * @throws [UnknownDomainObjectException] When the given object is not found.
 * @throws [IllegalArgumentException] When the given object cannot be cast to the expected type.
 */
fun <T : Any> NamedDomainObjectCollection<out Any>.getByName(name: String, type: KClass<T>): T =
    getByName(name).let {
        type.safeCast(it)
            ?: error("")//throw illegalElementType(this, name, type, it::class)
    }


/**
 * Locates an object by name and casts it to the expected [type] then configures it.
 *
 * If an object with the given [name] is not found, [UnknownDomainObjectException] is thrown.
 * If the object is found but cannot be cast to the expected [type], [IllegalArgumentException] is thrown.
 *
 * @param name object name
 * @param configure configuration action to apply to the object before returning it
 * @return the object, never null
 * @throws [UnknownDomainObjectException] When the given object is not found.
 * @throws [IllegalArgumentException] When the given object cannot be cast to the expected type.
 */
fun <T : Any> NamedDomainObjectCollection<out Any>.getByName(name: String, type: KClass<T>, configure: T.() -> Unit): T =
    getByName(name, type).also(configure)


/**
 * Locates an object by name and casts it to the expected type [T] then configures it.
 *
 * If an object with the given [name] is not found, [UnknownDomainObjectException] is thrown.
 * If the object is found but cannot be cast to the expected type [T], [IllegalArgumentException] is thrown.
 *
 * @param name object name
 * @param configure configuration action to apply to the object before returning it
 * @return the object, never null
 * @throws [UnknownDomainObjectException] When the given object is not found.
 * @throws [IllegalArgumentException] When the given object cannot be cast to the expected type.
 */
inline fun <reified T : Any> NamedDomainObjectCollection<out Any>.getByName(name: String, configure: T.() -> Unit) =
    getByName<T>(name).also(configure)


/**
 * Idiomatic way of referring to an existing element in a collection
 * via a delegate property.
 *
 * `tasks { val jar by getting }`
 */
inline val <T : Any, U : NamedDomainObjectCollection<out T>> U.getting
    get() = NamedDomainObjectCollectionDelegateProvider.of(this)


/**
 * Idiomatic way of referring and configuring an existing element in a collection
 * via a delegate property.
 *
 * `tasks { val jar by getting { group = "My" } }`
 */
fun <T : Any, U : NamedDomainObjectCollection<T>> U.getting(configuration: T.() -> Unit) =
    NamedDomainObjectCollectionDelegateProvider.of(this, configuration)


/**
 * Enables typed access to container elements via delegated properties.
 */
class NamedDomainObjectCollectionDelegateProvider<T : Any>
private constructor(
    internal val collection: NamedDomainObjectCollection<T>,
    internal val configuration: (T.() -> Unit)?
) {
    companion object {
        fun <T : Any> of(
            collection: NamedDomainObjectCollection<T>,
            configuration: (T.() -> Unit)? = null
        ) =
            NamedDomainObjectCollectionDelegateProvider(collection, configuration)
    }

    operator fun provideDelegate(thisRef: Any?, property: kotlin.reflect.KProperty<*>) = ExistingDomainObjectDelegate.of(
        when (configuration) {
            null -> collection.getByName(property.name)
            else -> collection.getByName(property.name, configuration)
        }
    )
}


/**
 * Locates an object by name, failing if there is no such object.
 *
 * @param name The object name
 * @return The object with the given name.
 * @throws [UnknownDomainObjectException] when there is no such object in this collection.
 *
 * @see [NamedDomainObjectCollection.getByName]
 */
operator fun <T : Any> NamedDomainObjectCollection<T>.get(name: String): T =
    getByName(name)


/**
 * Allows a [NamedDomainObjectCollection] to be used as a property delegate.
 *
 * @see [NamedDomainObjectCollection.named]
 */
operator fun <T : Any> NamedDomainObjectCollection<T>.provideDelegate(thisRef: Any?, property: KProperty<*>): NamedDomainObjectProvider<T> =
    named(property.name)


/**
 * Allows a [NamedDomainObjectProvider] to be used as a property delegate.
 *
 * @see [NamedDomainObjectProvider.get]
 */
@Suppress("nothing_to_inline", "unchecked_cast")
inline operator fun <T : Any, reified U : T> NamedDomainObjectProvider<out T>.getValue(thisRef: Any?, property: KProperty<*>): U =
    get().let {
        it as? U
            ?: error("")//throw illegalElementType(this, property.name, U::class, it::class)
    }


/**
 * Required due to [KT-25810](https://youtrack.jetbrains.com/issue/KT-25810).
 */
operator fun <T : Any> T.provideDelegate(receiver: Any?, property: KProperty<*>): T = this

/**
 * Returns a collection containing the objects in this collection of the given type.
 * Equivalent to calling `withType(type).all(configureAction)`.
 *
 * @param S The type of objects to find.
 * @param configuration The action to execute for each object in the resulting collection.
 * @return The matching objects. Returns an empty collection if there are no such objects
 * in this collection.
 * @see [DomainObjectCollection.withType]
 */
inline fun <reified S : Any> DomainObjectCollection<in S>.withType(noinline configuration: S.() -> Unit) =
    withType(S::class.java, configuration)


/**
 * Returns a collection containing the objects in this collection of the given type. The
 * returned collection is live, so that when matching objects are later added to this
 * collection, they are also visible in the filtered collection.
 *
 * @param S The type of objects to find.
 * @return The matching objects. Returns an empty collection if there are no such objects
 * in this collection.
 * @see [DomainObjectCollection.withType]
 */
inline fun <reified S : Any> DomainObjectCollection<in S>.withType(): DomainObjectCollection<S> =
    withType(S::class.java)


/**
 * Assign value: T to a property with assign operator
 *
 * @since 8.2
 */
fun <T : Any> Property<T>.assign(value: T?) {
    this.set(value)
}


/**
 * Assign value: Provider<T> to a property with assign operator
 *
 * @since 8.2
 */
fun <T : Any> Property<T>.assign(value: Provider<out T>) {
    this.set(value)
}


/**
 * Assign file to a FileSystemLocationProperty with assign operator
 *
 * @since 8.2
 */
fun <T : FileSystemLocation> FileSystemLocationProperty<T>.assign(file: File?) {
    this.set(file)
}


/**
 * Assign file provided by a Provider to a FileSystemLocationProperty with assign operator
 *
 * @since 8.2
 */
fun <T : FileSystemLocation> FileSystemLocationProperty<T>.assign(provider: Provider<File>) {
    this.fileProvider(provider)
}


/**
 * Sets the value of the property to the elements of the given iterable, and replaces any existing value
 *
 * @since 8.2
 */
fun <T : Any> HasMultipleValues<T>.assign(elements: Iterable<T>?) {
    this.set(elements)
}


/**
 * Sets the property to have the same value of the given provider, and replaces any existing value
 *
 * @since 8.2
 */
fun <T : Any> HasMultipleValues<T>.assign(provider: Provider<out Iterable<T>>) {
    this.set(provider)
}


/**
 * Sets the value of this property to the entries of the given Map, and replaces any existing value
 *
 * @since 8.2
 */
fun <K : Any, V : Any> MapProperty<K, V>.assign(entries: Map<out K, V>?) {
    this.set(entries)
}


/**
 * Sets the property to have the same value of the given provider, and replaces any existing value
 *
 * @since 8.2
 */
fun <K : Any, V : Any> MapProperty<K, V>.assign(provider: Provider<out Map<out K, V>>) {
    this.set(provider)
}


///**
// * Defines a new object, which will be created when it is required.
// *
// * @see [PolymorphicDomainObjectContainer.register]
// */
//@Suppress("extension_shadowed_by_member")
//inline fun <reified T : Any> PolymorphicDomainObjectContainer<in T>.register(name: String): NamedDomainObjectProvider<T> =
//    register(name, T::class.java)


/**
 * Defines and configure a new object, which will be created when it is required.
 *
 * @see [PolymorphicDomainObjectContainer.register]
 */
inline fun <reified T : Any> PolymorphicDomainObjectContainer<in T>.register(name: String, noinline configuration: T.() -> Unit): NamedDomainObjectProvider<T> =
    register(name, T::class.java, configuration)


/**
 * Creates a domain object with the specified name and type, adds it to the container,
 * and configures it with the specified action.
 *
 * @param name the name of the domain object to be created
 * @param configuration an action for configuring the domain object
 * @param <U> the type of the domain object to be created
 * @return the created domain object
 * @throws [InvalidUserDataException] if a domain object with the specified name already
 * exists or the container does not support creating a domain object with the specified
 * type
 */
inline fun <reified U : Any> PolymorphicDomainObjectContainer<in U>.create(
    name: String,
    noinline configuration: U.() -> Unit
) =

    this.create(name, U::class.java, configuration)


/**
 * Creates a domain object with the specified name and type, and adds it to the container.
 *
 * @param name the name of the domain object to be created
 * @param <U> the type of the domain object to be created
 * @return the created domain object
 * @throws [InvalidUserDataException] if a domain object with the specified name already
 * exists or the container does not support creating a domain object with the specified
 * type
 */
@Suppress("extension_shadowed_by_member")
inline fun <reified U : Any> PolymorphicDomainObjectContainer<in U>.create(name: String) =
    create(name, U::class.java)


/**
 * Creates a domain object with the specified name and type if it does not exists, and adds it to the container.
 *
 * @param name the name of the domain object to be created
 * @param <U> the type of the domain object to be created
 * @return the created domain object
 * @throws [InvalidUserDataException] if a domain object with the specified name already
 * exists or the container does not support creating a domain object with the specified
 * type
 * @throws [ClassCastException] if a domain object with the specified name exists with a different type
 */
@Suppress("extension_shadowed_by_member")
inline fun <reified U : Any> PolymorphicDomainObjectContainer<in U>.maybeCreate(name: String): U  =
    maybeCreate(name, U::class.java)

/**
 * The extra properties extension in this object's extension container.
 *
 * @see [ExtensionContainer.getExtraProperties]
 */
val ExtensionAware.extra: ExtraPropertiesExtension
    get() = extensions.extraProperties

/**
 * Locates a task by name and type, without triggering its creation or configuration, failing if there is no such task.
 *
 * @see [TaskCollection.named]
 */
@Suppress("extension_shadowed_by_member")
inline fun <reified T : Task> TaskCollection<out Task>.named(name: String): TaskProvider<T> =
    named(name, T::class)


/**
 * Locates a task by name and type, without triggering its creation or configuration, failing if there is no such task.
 *
 * @see [TaskCollection.named]
 */
@Suppress("unchecked_cast")
fun <T : Task> TaskCollection<out Task>.named(name: String, type: KClass<T>): TaskProvider<T> =
    (this as TaskCollection<T>).named(name, type.java)


/**
 * Configures a task by name and type, without triggering its creation or configuration, failing if there is no such task.
 *
 * @see [TaskCollection.named]
 * @see [TaskProvider.configure]
 */
@Suppress("unchecked_cast")
fun <T : Task> TaskCollection<out Task>.named(name: String, type: KClass<T>, configuration: T.() -> Unit): TaskProvider<T> =
    (this as TaskCollection<T>).named(name, type.java, configuration)


/**
 * Configures a task by name and type, without triggering its creation or configuration, failing if there is no such task.
 *
 * @see [TaskCollection.named]
 * @see [TaskProvider.configure]
 */
@Suppress("unchecked_cast")
inline fun <reified T : Task> TaskCollection<out Task>.named(name: String, noinline configuration: T.() -> Unit): TaskProvider<T> =
    (this as TaskCollection<T>).named(name, T::class.java, configuration)


/**
 * Defines a new task, which will be created when it is required.
 *
 * @see [TaskContainer.register]
 */
@Suppress("extension_shadowed_by_member")
inline fun <reified T : Task> TaskContainer.register(name: String): TaskProvider<T> =
    register(name, T::class.java)


/**
 * Defines and configure a new task, which will be created when it is required.
 *
 * @see [TaskContainer.register]
 */
inline fun <reified T : Task> TaskContainer.register(name: String, noinline configuration: T.() -> Unit): TaskProvider<T> =
    register(name, T::class.java, configuration)


/**
 * Defines a new task, which will be created when it is required passing the given arguments to the [javax.inject.Inject]-annotated constructor.
 *
 * @see [TaskContainer.register]
 */
inline fun <reified T : Task> TaskContainer.register(name: String, vararg arguments: Any): TaskProvider<T> =
    register(name, T::class.java, *arguments)

inline fun <reified S : Task> TaskCollection<in S>.withType(): TaskCollection<S> =
    withType(S::class.java)

/**
 * Creates a simple immutable [Named] object of the given type and name.
 *
 * @param T The type of object to create
 * @param name The name of the created object
 * @return the created named object
 *
 * @see [ObjectFactory.named]
 */
inline fun <reified T : Named> ObjectFactory.named(name: String): T =
    named(T::class.java, name)


/**
 * Create a new instance of `T`, using [parameters] as the construction parameters.
 *
 * @param T The type of object to create
 * @param parameters The construction parameters
 * @return the created named object
 *
 * @see [ObjectFactory.newInstance]
 */
inline fun <reified T> ObjectFactory.newInstance(vararg parameters: Any): T =
    newInstance(T::class.java, *parameters)


/**
 * Creates a [Property] that holds values of the given type [T].
 *
 * @see [ObjectFactory.property]
 */
inline fun <reified T> ObjectFactory.property(): Property<T> =
    property(T::class.java)


/**
 * Creates a [SetProperty] that holds values of the given type [T].
 *
 * @see [ObjectFactory.setProperty]
 */
inline fun <reified T> ObjectFactory.setProperty(): SetProperty<T> =
    setProperty(T::class.java)


/**
 * Creates a [ListProperty] that holds values of the given type [T].
 *
 * @see [ObjectFactory.listProperty]
 */
inline fun <reified T> ObjectFactory.listProperty(): ListProperty<T> =
    listProperty(T::class.java)


/**
 * Creates a [MapProperty] that holds values of the given key type [K] and value type [V].
 *
 * @see [ObjectFactory.mapProperty]
 */
inline fun <reified K, reified V> ObjectFactory.mapProperty(): MapProperty<K, V> =
    mapProperty(K::class.java, V::class.java)
