package tmg.testutils.junit

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.Exception

sealed class TestModel {
    object X: TestModel()
    object Y: TestModel()
    object Z: TestModel()
}

sealed class Custom {
    data class Help(
        val x: Int
    ): Custom()
    object Me: Custom()
}

internal class SealedClassSourceTest {

    @Test
    fun `converting string to sealed class instance works as expected`() {
        val x: TestModel = "X".toSealedClass(TestModel::class)
        val y: TestModel = "Y".toSealedClass(TestModel::class)
        val z: TestModel = "Z".toSealedClass(TestModel::class)

        assertEquals(TestModel.X, x)
        assertEquals(TestModel.Y, y)
        assertEquals(TestModel.Z, z)
    }

    @Test
    fun `converting string to sealed class custom data class works as expected with default params`() {
        val help: Custom = "Help".toSealedClass(Custom::class)
        val me: Custom = "Me".toSealedClass(Custom::class)

        assertEquals(Custom.Help(0), help)
        assertEquals(Custom.Me, me)
    }

    @Test
    fun `converting unknown string to sealed class custom data throws exception`() {
        assertThrows<Exception> {
            val a: TestModel = "A".toSealedClass(TestModel::class)
        }
    }
}