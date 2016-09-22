package fr.gstraymond.task

import fr.gstraymond.indexer.{EsAutocompleteIndexer, EsCardIndexer}

object DeleteCardIndexTask extends Task[Unit]{
  override def process = EsCardIndexer.delete()
}

object ConfigureCardIndexTask extends Task[Unit]{
  override def process = EsCardIndexer.configure()
}

object CardIndexTask extends Task[Unit]{
  override def process = EsCardIndexer.index(loadMTGCards)
}

object CardExistsTask extends Task[Seq[String]]{
  override def process = EsCardIndexer.exists(loadMTGCards)
}

object DeleteAutocompleteIndexTask extends Task[Unit]{
  override def process = EsAutocompleteIndexer.delete()
}

object ConfigureAutocompleteIndexTask extends Task[Unit]{
  override def process = EsAutocompleteIndexer.configure()
}

object AutocompleteIndexTask extends Task[Unit]{
  override def process = EsAutocompleteIndexer.index(loadMTGCards)
}