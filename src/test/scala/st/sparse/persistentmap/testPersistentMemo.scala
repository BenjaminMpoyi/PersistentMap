package st.sparse.persistentmap

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.FunSuite
import org.scalacheck.Gen
import scala.pickling._
import scala.pickling.binary._
import scala.slick.session.Database
import java.io.File

@RunWith(classOf[JUnitRunner])
class TestPersistentMemo extends FunSuite with GeneratorDrivenPropertyChecks {
  // Creates a MySQL database, backed by some random temporary file.
  def createSQLiteDatabase = {
    val tempFile = File.createTempFile("testPersistentMapDB", ".sqlite")
    ConnectionHelper.databaseSQLite(tempFile)
  }

  // This will only work in a Travis CI environment.
  def createMySQLDatabase = ConnectionHelper.databaseMySQL(
    "localhost",
    "myapp_test",
    "travis",
    "")

  test("easy test") {
    val database: scala.slick.session.Database = createSQLiteDatabase

    var count = 0
    def sideEffectFoo(string: String): Int = {
      count += 1
      string.size
    }

    val memo = PersistentMemo(database, "sideEffectFoo", sideEffectFoo _)

    assert(memo("abcd") == 4)
    // Not memoized, so should increment count.
    assert(count == 1)

    assert(memo("hi") == 2)
    // Not memoized, so should increment count.
    assert(count == 2)

    assert(memo("abcd") == 4)
    // Memoized, so should not increment count.
    assert(count == 2)

    assert(memo("ah") == 2)
    // Not memoized, so should increment count.
    assert(count == 3)
  }

  test("reconnection test") {
    val database: scala.slick.session.Database = createSQLiteDatabase

    var count = 0
    def sideEffectFoo(string: String): Int = {
      count += 1
      string.size
    }
    
    val memo1 = PersistentMemo(database, "reconnectionTest", sideEffectFoo _)
    
    assert(memo1("abcd") == 4)
    // Not memoized, so should increment count.
    assert(count == 1)
    
    val memo2 = PersistentMemo(database, "reconnectionTest", sideEffectFoo _)
    
    assert(memo2("abcd") == 4)
    // Memoized, so should not increment count.
    assert(count == 1)
  }
}