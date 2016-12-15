cp /Users/sunjian/Documents/workspace/elasticsearch-analysis-ansj/target/releases/elasticsearch-analysis-ansj-2.x.0-release.zip /Users/sunjian/Documents/src/elasticsearch-2.3.1/plugins


cd /Users/sunjian/Documents/src/elasticsearch-2.3.1/plugins/
rm -rf elasticsearch-analysis-ansj


unzip elasticsearch-analysis-ansj-2.x.0-release.zip


mv elasticsearch-analysis-ansj-2.x.0  elasticsearch-analysis-ansj


rm -rf elasticsearch-analysis-ansj-2.x.0-release.zip


cd /Users/sunjian/Documents/src/elasticsearch-2.3.1/bin/

./elasticsearch


