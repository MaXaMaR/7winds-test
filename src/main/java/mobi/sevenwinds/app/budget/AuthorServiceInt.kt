package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object AuthorServiceInt {

    suspend fun findRecord(fullName: String): AuthorEntity? = withContext(Dispatchers.IO) {
        transaction {
            return@transaction AuthorTable
                .select { AuthorTable.fullName eq fullName }
                .map { AuthorEntity.wrapRow(it) }
                .lastOrNull()
        }
    }

}
