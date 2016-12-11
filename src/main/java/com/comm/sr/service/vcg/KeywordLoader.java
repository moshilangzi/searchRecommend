package com.comm.sr.service.vcg;

import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.wltea.analyzer.dic.Word;
import org.wltea.analyzer.dic.WordsLoader;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Created by jasstion on 07/12/2016.
 */
public class KeywordLoader implements WordsLoader {

  @Override
  public Collection<Word> load() {
    Collection<Word> words = Lists.newArrayList();
    //读取扩展词典文件
    String filePath="/data/apps/dataTools/kwInfo.csv";
    List<String> lines= null;
    try {
      lines = FileUtils.readLines(new File(filePath));
    } catch (IOException e) {
      e.printStackTrace();
    }
    for(String line:lines){
      //id+"@"+enname+"@"+cnname+"@"+cnsyno+"@"+ensyno,path

      String[] strs=line.split("@");
      if(strs.length<5){
        continue;
      }
      String id=strs[0];
      String enname=strs[1];
      String cnname=strs[2];
      String cnsyno=strs[3];
      String ensyno=strs[4];
      int id_=Integer.parseInt(id);
     // System.out.print(id_+"\n");
      if(enname.length()>0){
        words.add(new Word(enname,id_));
      }
      if(cnname.length()>0){
        words.add(new Word(cnname,id_));
      }
      if(cnsyno.length()>0){
        String[] cnsynos=cnsyno.split(";");
        if(cnsynos.length>0){
          for(String str:cnsynos){
            words.add(new Word(str,id_));
          }

        }

      }
      if(ensyno.length()>0){
        String[] cnsynos=ensyno.split(";");
        if(cnsynos.length>0){
          for(String str:cnsynos){

            words.add(new Word(str,id_));
          }

        }
      }





    }


    return words;

  }
}
