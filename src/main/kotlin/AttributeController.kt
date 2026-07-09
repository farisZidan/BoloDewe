package com.bolodewe

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object AttributeController {

    suspend fun getAllAttributes(call: ApplicationCall) {
        val attributes = transaction {
            AllAttributesDTO(
                categories = CategoriesTable.selectAll().map { AttributeItemDTO(it[CategoriesTable.idCategory], it[CategoriesTable.name]) },
                materials = MaterialsTable.selectAll().map { AttributeItemDTO(it[MaterialsTable.idMaterial], it[MaterialsTable.name]) },
                brands = BrandsTable.selectAll().map { AttributeItemDTO(it[BrandsTable.idBrand], it[BrandsTable.name]) },
                size_groups = SizeGroupsTable.selectAll().map { AttributeItemDTO(it[SizeGroupsTable.idSize], it[SizeGroupsTable.name]) },
                genders = GendersTable.selectAll().map { AttributeItemDTO(it[GendersTable.idGender], it[GendersTable.name]) },
                cuttings = CuttingsTable.selectAll().map { AttributeItemDTO(it[CuttingsTable.idCutting], it[CuttingsTable.name]) },
                fits = FitsTable.selectAll().map { AttributeItemDTO(it[FitsTable.idFit], it[FitsTable.name]) },
                colors = ColorsTable.selectAll().map { AttributeItemDTO(it[ColorsTable.idColor], it[ColorsTable.name]) },
                age_groups = AgeGroupsTable.selectAll().map { AttributeItemDTO(it[AgeGroupsTable.idAge], it[AgeGroupsTable.name]) }
            )
        }
        call.respond(HttpStatusCode.OK, attributes)
    }
}