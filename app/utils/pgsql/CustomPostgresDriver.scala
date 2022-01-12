package utils.pgsql

import com.github.tminglei.slickpg.PgEnumSupport
import slick.jdbc.PostgresProfile
import models.Categories

trait CustomPostgresProfile extends PostgresProfile with PgEnumSupport {
  override val api: API = new CategoryImplicitsAPI {}

  trait CategoryImplicitsAPI extends super.API{
    implicit val categoryTypeMapper = createEnumJdbcType("category", Categories)
    implicit val categoryListTypeMapper = createEnumListJdbcType("category", Categories)

    implicit val categoryColumnExtensionMethodsBuilder =
      createEnumColumnExtensionMethodsBuilder(Categories)

    implicit val categoryOptionColumnExtensionMethodsBuilder =
      createEnumOptionColumnExtensionMethodsBuilder(Categories)
  }
}

object CustomPostgresProfile extends CustomPostgresProfile