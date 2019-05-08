package fr.gstraymond.task

import fr.gstraymond.indexer.{EsAutocompleteIndexer, EsCardIndexer, EsRulesIndexer}

object DeleteCardIndexTask extends Task[Unit]{
  override def process = EsCardIndexer.delete()
}

object ConfigureCardIndexTask extends Task[Unit]{
  override def process = EsCardIndexer.configure()
}

object CardIndexTask extends Task[Unit]{
  override def process = EsCardIndexer.index(loadMTGCards)
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

object DeleteRulesIndexTask extends Task[Unit]{
  override def process = EsRulesIndexer.delete()
}

object ConfigureRulesIndexTask extends Task[Unit]{
  override def process = EsRulesIndexer.configure()
}

object RulesIndexTask extends Task[Unit]{
  override def process = EsRulesIndexer.index(loadRules)
}