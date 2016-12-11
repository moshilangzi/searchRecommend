package com.comm.sr.service.vcg;

import com.comm.sr.common.component.AbstractComponent;
import com.google.common.collect.Lists;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.wltea.analyzer.dic.Dictionary;
import org.wltea.analyzer.dic.IKMatchOperation;
import org.wltea.analyzer.dic.MatchOperation;
import org.wltea.analyzer.dic.WordsLoader;
import org.wltea.analyzer.lucene.IKTokenizer;

import java.io.StringReader;
import java.util.List;
import java.util.Properties;

/**
 * Created by jasstion on 07/12/2016.
 */
public class KeywordService extends AbstractComponent{

  public KeywordService(Properties settings) {
    super(settings);
  }
  final static class KwInfo{
    public String getKwId() {
      return kwId;
    }

    public void setKwId(String kwId) {
      this.kwId = kwId;
    }

    public String getText() {
      return text;
    }

    public void setText(String text) {
      this.text = text;
    }

    private String text=null;
    private List<String> kwIds=Lists.newArrayList();

    public KwInfo(String kwId, String text) {
      super();
      this.kwId = kwId;
      this.text = text;
    }
    public KwInfo(List<String> kwIds, String text) {
      super();
      this.kwIds = kwIds;
      this.text = text;
    }

    public List<String> getKwIds() {
      return kwIds;
    }

    public void setKwIds(List<String> kwIds) {
      this.kwIds = kwIds;
    }

    @Override public String toString() {
      return "KwInfo{" +
          "kwId='" + kwId + '\'' +
          ", text='" + text + '\'' +
          ", kwIds=" + kwIds +
          '}';
    }

    @Override public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof KwInfo)) return false;

      KwInfo kwInfo = (KwInfo) o;

      if (!text.equals(kwInfo.text)) return false;
      return kwId.equals(kwInfo.kwId);

    }

    @Override public int hashCode() {
      int result = text.hashCode();
      result = 31 * result + kwId.hashCode();
      return result;
    }

    private String kwId=null;


  }
  public List<KwInfo> parseInputText(String inputText){
    IKTokenizer iKTokenizer = new IKTokenizer(new StringReader(inputText), true);
    List<KwInfo>  kwInfos= Lists.newArrayList();
    try {
      while (iKTokenizer.incrementToken()) {
        CharTermAttribute charTermAttribute = iKTokenizer.getAttribute(CharTermAttribute.class);
        TypeAttribute typeAttribute = iKTokenizer.getAttribute(TypeAttribute.class);
        String term = charTermAttribute.toString();
        String additionInfo = typeAttribute.type();
        logger.debug(additionInfo);
        String kwId=null;
        String[] adds=additionInfo.split("_");
        List<String> kwIds=Lists.newArrayList();
        if(additionInfo!=null&adds.length>1){
          for(int i=1;i<adds.length;i++){
            kwIds.add(adds[i]);


          }
        }

        KwInfo kwInfo = new KwInfo(kwIds, term);
        kwInfos.add(kwInfo);

      }
    }catch (Exception e){
      logger.info("error to parse input text:"+inputText+"");
    }
    logger.info(kwInfos.toString());
    return kwInfos;


  }
  public static void main(String[] args) throws Exception{
    WordsLoader wordsLoader = new KeywordLoader();
    MatchOperation matchOperation = new IKMatchOperation();

    Dictionary dictionary = new Dictionary(wordsLoader, matchOperation);
    Dictionary.setDictionary(dictionary);
    String inputText="蓝天白云海滩城市沙漠海滩洗浴巾;蓝天白云;白云;云朵;;天空白云;;天空云朵;;云海;云彩;云端;云层";
    KeywordService keywordService=new KeywordService(null);
    keywordService.parseInputText(inputText);







  }
}
