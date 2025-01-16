package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object BudgetService {
    suspend fun addRecord(body: BudgetRecord): BudgetRecord = withContext(Dispatchers.IO) {
        transaction {
            val entity = BudgetEntity.new {
                this.year = body.year
                this.month = body.month
                this.amount = body.amount
                this.type = body.type
                this.author = if (body.author != null) AuthorEntity.findById(body.author.id) else null
            }
            return@transaction entity.toResponse()
        }
    }

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        transaction {
            val query = (BudgetTable leftJoin AuthorTable)
                .select { BudgetTable.year eq param.year }
                .orderBy(BudgetTable.month, SortOrder.ASC)
                .orderBy(BudgetTable.amount, SortOrder.DESC)
                .limit(param.limit, param.offset)

            val total = query.count()
            var data = BudgetEntity.wrapRows(query).map { it.toResponse() }

            if (param.authorFullName != null) {
                val authorFullNameComp = param.authorFullName.toLowerCase()
                val authorFullNameParts = authorFullNameComp.split(' ')
                data = data.filter {
                    val itFullName = it.author?.fullName?.toLowerCase()
                    authorFullNameParts.all { authorFullNamePart ->
                        itFullName?.contains(authorFullNamePart) == true
                    }
                }
            }

            val sumByType = data.groupBy { it.type.name }.mapValues { it.value.sumOf { v -> v.amount } }

            return@transaction BudgetYearStatsResponse(
                total = total,
                totalByType = sumByType,
                items = data
            )
        }
    }
}
