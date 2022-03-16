package com.mauter.oauth2server

import io.kotest.core.spec.style.StringSpec
import org.assertj.core.api.Assertions.assertThat
import org.hibernate.Session
import org.springframework.beans.factory.annotation.Autowired
import javax.persistence.EntityManager

abstract class AbstractEntityTest(body: StringSpec.() -> Unit = {}) : StringSpec(body) {

    @Autowired
    lateinit var entityManager: EntityManager

    @Throws(Exception::class)
    fun doInJpa(doer: () -> Unit) {
        val transaction = entityManager.transaction
        try {
            transaction.begin()
            doer.invoke()
            transaction.commit()
        } catch (e: Exception) {
            try {
                transaction.rollback()
            } catch (inner: Exception) {
                // don't care
            }
            throw e
        }
    }

    @Throws(Exception::class)
    fun <T> doInJpaAndReturn(doer: () -> T): T {
        val transaction = entityManager.transaction
        try {
            transaction.begin()
            val result = doer.invoke()
            transaction.commit()
            return result
        } catch (e: Exception) {
            try {
                transaction.rollback()
            } catch (inner: Exception) {
                // don't care
            }
            throw e
        }
    }

    protected fun <T : AbstractBaseEntity> assertEqualityConsistency(clazz: Class<T>, entity: T) {
        val tuples = mutableSetOf<T>()

        assertThat(tuples).doesNotContain(entity)
        tuples.add(entity)
        assertThat(tuples).contains(entity)

        doInJpa {
            entityManager.persist(entity)
            entityManager.flush()
            assertThat(tuples)
                .contains(entity)
                .withFailMessage("The entity is not found in the Set after it's persisted.")
        }

        doInJpa {
            val entityProxy = entityManager.getReference(clazz, entity.id)
            assertThat(entityProxy)
                .isEqualTo(entity)
                .withFailMessage("The entity proxy is not equal with the entity.")
        }

        doInJpa {
            val entityProxy = entityManager.getReference(clazz, entity.id)
            assertThat(entity)
                .isEqualTo(entityProxy)
                .withFailMessage("The entity is not equal with the entity proxy.")
        }

        doInJpa {
            val merged = entityManager.merge(entity)
            assertThat(tuples)
                .contains(merged)
                .withFailMessage("The entity is not found in the Set after it's merged.")
        }

        doInJpa {
            entityManager.unwrap(Session::class.java).update(entity)
            assertThat(tuples)
                .contains(entity)
                .withFailMessage("The entity is not found in the Set after it's reattached.")
        }

        doInJpa {
            val found = entityManager.find(clazz, entity.id)
            assertThat(tuples)
                .contains(found)
                .withFailMessage("The entity is not found in the Set after it's loaded in a different Persistence Context.")
        }

        doInJpa {
            val entityProxy = entityManager.getReference(clazz, entity.id)
            assertThat(tuples)
                .contains(entityProxy)
                .withFailMessage("The entity is not found in the Set after it's loaded as a proxy in a different Persistence Context.")
        }

        val deleted = doInJpaAndReturn {
            val entityProxy = entityManager.getReference(clazz, entity.id)
            entityManager.remove(entityProxy)
            entityProxy
        }
        assertThat(tuples)
            .contains(deleted)
            .withFailMessage("asdf")
    }
}
