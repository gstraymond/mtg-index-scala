package fr.gstraymond.task

import fr.gstraymond.indexer.EsIndexer

object DeleteIndexTask extends Task[Unit]{
  override def process = EsIndexer.delete()
}

object ConfigureIndexTask extends Task[Unit]{
  override def process = EsIndexer.configure()
}

object CardIndexTask extends Task[Unit]{
  override def process = EsIndexer.index(loadMTGCards)
}
