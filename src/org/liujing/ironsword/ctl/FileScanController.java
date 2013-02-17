package org.liujing.ironsword.ctl;

import java.util.*;
import java.io.*;
import java.util.zip.*;
import java.util.logging.*;
import java.sql.*;
import java.util.regex.*;
import org.liujing.ironsword.dao.*;
import liujing.util.dirscan.*;
import org.liujing.ironsword.bean.*;
import org.liujing.ironsword.lang.*;
import org.liujing.ironsword.IronException;
import org.antlr.runtime.ANTLRStringStream;
import org.liujing.magdown.ctl.net.WebToolkit;
import org.liujing.magdown.ctl.ResponseStreamHandler2;
import org.liujing.magdown.DownloadException;
import java.net.*;
import org.directwebremoting.annotations.*;

@RemoteProxy(scope=ScriptScope.SESSION)
public class FileScanController{
    private Logger log = Logger.getLogger(FileScanController.class.getName());
    private String name;
    private RootFolder currRootFolder;
    private WebToolkit webClient;
    private WebResponseHandler webResponseHandler;
    private ZipFileController zipController = new ZipFileController();
    private JavaModelBuilder javaBuilder = new JavaModelBuilder();
    //private MyScanHandler scanhandler;
    
    public FileScanController(){
        webClient = new WebToolkit("liujing");
    }
    
    public FileScanController(String name){
        this.name = name;
    }
    
    @RemoteMethod
    public DaoPagination dirRootFolders(final PagingRequest pr){
        return DBWorker.getInstance().execute(new SQLCommand<DaoPagination>(){
                    public DaoPagination run(Connection conn)throws Exception{
                        return RootFolder.listAll(conn, pr);
                    }
        });
    }
    
    public SubFilesVO expandFilesFoldersByPath(final int folderIdx, final String path ,
        final PagingRequest pr)
    {
        return DBWorker.getInstance().execute(new SQLCommand<SubFilesVO>(){
                public SubFilesVO run(Connection conn)throws Exception{
                    RootFolder rootFolder = RootFolder.findByIndex(conn, folderIdx);
                    if(rootFolder == null)
                        throw new IronException("Folder of specific index does not exist");
                    
                    return ProjectController.expandFilesFoldersByPath(conn, rootFolder, pr, path);
                }
        });
    }
    public SubFilesVO expandFilesFoldersByPath(final RootFolder rootFolder, final String path ,
        final PagingRequest pr)
    {
        return DBWorker.getInstance().execute(new SQLCommand<SubFilesVO>(){
                public SubFilesVO run(Connection conn)throws Exception{
                    return ProjectController.expandFilesFoldersByPath(conn, rootFolder, pr, path);
                }
        });
    }
    
    public SubFilesVO dir(final int rootFolderId, final String path, 
        final PagingRequest pr)
    {
        return DBWorker.getInstance().execute(new SQLCommand<SubFilesVO>(){
                public SubFilesVO run(Connection conn)throws Exception{
                    SubFilesVO vo = new SubFilesVO(pr);
                    RootFolder rootFolder = new RootFolder();
                    rootFolder.setId(rootFolderId);
                    DaoPagination page = rootFolder.listFileByPath(conn, path, pr);
                    vo.setSubSrcFiles(page);
                    int size = page.getData().length;
                    if(page.hasMore())
                        vo.setMore(true);
                    else{
                        int offset2 = pr.getOffset() - page.getTotal();
                        if(offset2 < 0)
                            offset2 = 0;
                        int limit2 = pr.getLimit() - size;
                        DaoPagination page2 = new DaoPagination("Sub-folders", offset2, limit2);
                        rootFolder.fetchFileTree(conn, path, true,page2);
                        vo.setSubFolders(page2);
                        vo.setMore(page2.hasMore());
                    }
                    return vo;
                }
                
        });
    }
    
    /**  list packages or files under a specific package
     @param rootDirIndex the index number of root folders, starts from 0
     @param path null if you want to list all folders recursively
    */
    public SubFilesVO dir(final int rootDirIndex, final String path, 
        final int offset, final int limit)
    {
        return DBWorker.getInstance().execute(new SQLCommand<SubFilesVO>(){
                public SubFilesVO run(Connection conn)throws Exception{
                    SubFilesVO vo = new SubFilesVO(offset, limit);
                    RootFolder rootFolder = RootFolder.findByIndex(conn, rootDirIndex);
                    if(rootFolder != null){
                        setCurrRootFolder(rootFolder);
                        System.out.println("[" + rootFolder + "] " + path);
                        DaoPagination page = new DaoPagination("Sub-files", offset, limit);
                        rootFolder.listFileByPath(conn, path, page);
                        vo.setSubSrcFiles(page);
                        int size = page.getData().length;
                        if(page.hasMore())
                            vo.setMore(true);
                        else{
                            int offset2 = offset - page.getTotal();
                            if(offset2 < 0)
                                offset2 = 0;
                            int limit2 = limit - size;
                            DaoPagination page2 = new DaoPagination("Sub-folders", offset2, limit2);
                            rootFolder.fetchFileTree(conn, path, true,page2);
                            vo.setSubFolders(page2);
                            vo.setMore(page2.hasMore());
                        }
                        
                    }else{
                        System.out.println("Folder of specific index does not exist, index: ");
                    }
                    return vo;
                }
                
        });
    }
    
    public void setCurrRootFolder(RootFolder rf){
        this.currRootFolder = rf;
    }
    
    public int addRootFolder(String rootPath, String include, String exclude){
      final RootFolder rf = new RootFolder();
      rf.setPath(rootPath);
      if(include != null)
          rf.setIncludes(new PathPatternParser(new PathPatternLexer(include)).parse());
      if(exclude != null)
          rf.setExcludes(new PathPatternParser(new PathPatternLexer(exclude)).parse());
      int count = DBWorker.getInstance().execute(new SQLCommand<Integer>(){
          public Integer run(Connection conn)throws Exception{
            rf.save(conn);
            return 1;
          }
      });
      return count;
    }
    
    public boolean updateRootFolder(final String rootPath, final String include, final String exclude){
      return DBWorker.getInstance().execute(new SQLCommand<Boolean>(){
          public Boolean run(Connection conn)throws Exception{
            RootFolder rf = RootFolder.findByPath(conn, rootPath);
            boolean newly = false;
            if(rf == null){
                newly = true;
                rf = new RootFolder();
                
            }
            rf.setPath(rootPath);
            rf.setIncludes(include != null? new PathPatternParser(new PathPatternLexer(include)).parse()
                : null);
            rf.setExcludes(exclude != null? new PathPatternParser(new PathPatternLexer(exclude)).parse()
                : null);
            rf.save(conn);
            
            return newly;
            
          }
      });
    }
    
    public boolean updateRootFolder(final int rootFolderId, final String rootPath, final String include, final String exclude){
      return DBWorker.getInstance().execute(new SQLCommand<Boolean>(){
          public Boolean run(Connection conn)throws Exception{
            RootFolder rf = RootFolder.getById(conn, rootFolderId);
            boolean newly = false;
            if(rf == null){
                newly = true;
                rf = new RootFolder();
                
            }
            rf.setPath(rootPath);
            rf.setIncludes(include != null? new PathPatternParser(new PathPatternLexer(include)).parse()
                : null);
            rf.setExcludes(exclude != null? new PathPatternParser(new PathPatternLexer(exclude)).parse()
                : null);
            rf.save(conn);
            
            return newly;
            
          }
      });
    }
    /**  rescan
     @param rootFolderIdx rootFolderIdx starts from 0
     @param coveredPath coveredPath
     @return int
    */
    public ScanResultVO rescan(final int rootFolderIdx, final String coveredPath){
        RootFolder rf = DBWorker.getInstance().execute(new SQLCommand<RootFolder>(){
                public RootFolder run(Connection conn)throws Exception{
                    RootFolder rf = RootFolder.findByIndex(conn, rootFolderIdx);
                    
                    return rf;
                }
        });
        if(rf == null){
            throw new IronException(String.format("Root Folder of index %1$s not found", rootFolderIdx));
            
        }
        return rescan(rf, coveredPath);
    }
    
    public ScanResultVO rescan(final RootFolder rf, final String coveredPath){
      final DirectoryScan3 scanner = new DirectoryScan3();
      scanner.setIncludes(rf.getIncludes());
      scanner.setExcludes(rf.getExcludes());
      return DBWorker.getInstance().execute(new SQLCommand<ScanResultVO>(){
           public ScanResultVO run(Connection conn)throws Exception{
              FileTree rootTree = rf.findFileTree(conn, "");
              if(rootTree == null){
                  rootTree = new FileTree();
                  rootTree.setPath("");
                  rootTree.setRootFolder(rf);
                  rootTree.save(conn);
              }
              SrcFile.clearTemp(conn);
              FileTree.clearTemp(conn);
              MyScanHandler h = new MyScanHandler(conn, rf);
              File rootFolderFile = new File(rf.getPath());
              //if(rootFolderFile.isDirectory())
                  scanner.scan(rootFolderFile, coveredPath, h);
              //else if(isZipFile(rootFolderFile)){
              //    scanZip(rootFolderFile);
              //}
              rf.setScanDate(new Timestamp(new java.util.Date().getTime()));
              rf.save(conn);
              int delCount = rf.deleteNotInTemp(conn, coveredPath);
              return new ScanResultVO(h.addedCount, h.updatedCount, delCount);
           }
      });
    }
    
    /*private boolean isZipFile(File f){
        if(f.getName().length()>4 ){ 
            String suffix = 
                f.getName().substring(f.getName().length() -4);
            if(suffix.equalsIgnoreCase(".zip") || suffix.equalsIgnoreCase(".jar")){
                return true;
            }
            return false;
        }
        return false;
    }*/
        
    
    private class MyScanHandler implements ScanHandler3{
        private Connection conn;
        private RootFolder rf;
        private FileTree currFolder;
        public int updatedCount;
        public int addedCount;
        public int duplicateCount;
    
        public MyScanHandler(Connection conn, RootFolder rf){
            this.conn = conn;
            this.rf = rf;
            
        }
        
        public void processFile(File f, String relativePath){
            storeFileRecord(f.getName(), relativePath, f.lastModified(), f);
        }
        
        public void processZipEntry(ZipInputStream zin, ZipEntry entry, String relativePath)
        {
            //System.out.println("zip entry:"+ relativePath);
            if(entry.isDirectory())
                return;
            int slashPos = entry.getName().lastIndexOf("/");
            slashPos = slashPos >=0? slashPos + 1: 0;
            String fileName = entry.getName().substring(slashPos);
            storeFileRecord(fileName, relativePath, entry.getTime(), null,SrcFile.IN_ZIP_FLAG);
        }
        
        /**  storeFileRecord
         @param fileName fileName
         @param relativePath relativePath
         @param lastModiTime set a nagetive value if there is no time information
         @param flags flags
        */
        protected void storeFileRecord(String fileName, String relativePath, 
            long lastModiTime, File f, int... flags){
            try{
                String rePath = relativePath.replaceAll("\\\\","/");
                
                String parentPath = rePath.substring(0, rePath.length() - fileName.length());
                if(this.currFolder == null || !parentPath.equals(this.currFolder.getPath() + "/")){
                    String folderPath = parentPath.length() == 0? "" :
                        parentPath.substring(0, parentPath.length() -1);
                    this.currFolder = this.rf.getFileTree(conn, folderPath);
                    if(this.currFolder == null){
                        this.currFolder = new FileTree();
                        this.currFolder.setPath(folderPath);
                        //this.currFolder.setNumFiles(1);
                        log.fine("create folder =" + this.currFolder.getPath());
                        this.currFolder.setRootFolder(this.rf);
                        this.currFolder.save(conn);
                    }
                }
                //this.currFolder.increaseFile(this.conn);
                
                int dot = fileName.lastIndexOf(".");
                String suffix = dot >=0? fileName.substring(dot + 1): "";
                if(suffix.length() > 9){
                    suffix = suffix.substring(0, 9);
                }
                //if(suffix == null){
                //    return;
                //}
                SrcFile scf = currFolder.findSrcFile(conn, fileName);
                if(scf == null){
                    scf = new SrcFile();
                    scf.setName(fileName);
                    scf.setPackageName("");
                    scf.setSrcType(suffix);
                    
                    scf.setFileTree(this.currFolder);
                    addedCount ++;
                    recognizeLanguage(f, scf, suffix);
                    log.fine("Add:\t"+ scf);
                }else if(scf.getLastModified()== null || lastModiTime > scf.getLastModified().getTime()){
                        log.fine("Update:\t"+ scf);
                        recognizeLanguage(f, scf, suffix);
                        updatedCount ++;
                }
                for(int flag : flags)
                    scf.setCheckFlagBit(flag);
                if(lastModiTime > 0)
                    scf.setLastModified(new Timestamp(lastModiTime));
                //scf.setCheckFlag(scf.UPDATED_FLAG);
                scf.save(this.conn, this.currFolder);
                if(scf.markInTemp(conn)){
                    log.info(String.format("duplicate file found: %1$s/%2$s",
                        currFolder.getPath(), scf.getName()));
                    duplicateCount++;
                }
                
            }catch(SQLException e){
                throw new IronException("", e);
            }
        }
        
        private void recognizeLanguage(File f, SrcFile srcFile, String suffix){
            if(f == null)
                return; // it is a zip
            if("java".equals(suffix)){
                srcFile.setPackageName(javaBuilder.recognizePackage(f));
            }
        }
    }
    
    private class PathPatternLexer{
        ANTLRStringStream input;
        StringBuilder sb = new StringBuilder();
        public PathPatternLexer(String s){
            this.input = new ANTLRStringStream(s);
        }
        
        public String next(){
            sb.setLength(0);
            while(true){
                int c = input.LA(1);
                if(c == input.EOF)
                    break;
                else if(c == ',' || c == ';'){
                    input.consume();
                    return ";";
                }else if(c == '{'){
                    return brace();
                }else{
                    return other();
                }
               
            }
            return null;
        }
        /** '{' (~'}')*  '}' */
        private String brace(){
            sb.append("{");
            input.consume();
            while(input.LA(1) != '}'){
                if(input.LA(1) == input.EOF)
                    break;
                sb.append((char)input.LA(1));
                input.consume();
            }
            if(input.LA(1) == '}'){                
                sb.append((char)input.LA(1));
                input.consume();
            }
            return sb.toString();
        }
        
        /** ( ~('{'|'}'|','|';') )* */
        private String other(){
            int c = input.LA(1);
            while(c != '{' && c != '}' && c != ',' && c != ';'){
                if(c == input.EOF)
                    break;
                sb.append((char)c);
                input.consume();
                c = input.LA(1);
            }
            return sb.toString();
        }
    }
        
    private class PathPatternParser{
        PathPatternLexer lex;
        public PathPatternParser(PathPatternLexer lexer){
            lex = lexer;
        }
        
        public List<String> parse(){
            List<String> list = new ArrayList();
            StringBuilder sb = new StringBuilder();
            String s = lex.next();
            while(s != null){
                log.fine("token: "+ s  );
                if(s.equals(";")){
                    list.add(sb.toString().trim());
                    sb.setLength(0);
                }else{
                    sb.append(s);
                }
                s = lex.next();
            }
            String last = sb.toString();
            if(last.length() > 0)
                list.add(sb.toString().trim());
            return list;
        }
    }
        
    public int deleteRootFolder(final int rootDirIndex){
        return DBWorker.getInstance().execute(new SQLCommand<Integer>(){
              public Integer run(Connection conn)throws Exception{
                  RootFolder rf = RootFolder.findByIndex(conn, rootDirIndex -1);
                  if(rf != null){
                      return rf.delete(conn);
                  }else{
                      return 0;
                  }
              }
        });
    }
    
    public int deleteRootFolderById(final int id){
        return DBWorker.getInstance().execute(new SQLCommand<Integer>(){
              public Integer run(Connection conn)throws Exception{
                  RootFolder rf = RootFolder.getById(conn, id);
                  if(rf != null){
                      return rf.delete(conn);
                  }else{
                      return 0;
                  }
              }
        });
    }
    
    public FileTree getFileTree_RootFolder(final int fileTreeId){
        return DBWorker.getInstance().execute(new SQLCommand<FileTree>(){
            public FileTree run(Connection conn)throws Exception{
                  return FileTree.getById(conn, fileTreeId);
              }
        });
    }
    
    public RootFolder getRootFolder(final int rootFolderId){
        return DBWorker.getInstance().execute(new SQLCommand<RootFolder>(){
            public RootFolder run(Connection conn)throws Exception{
                  return RootFolder.getById(conn, rootFolderId);
              }
        });
    }
    
    public SrcFile getSrcFile(final int srcFileId){
        
        return DBWorker.getInstance().execute(new SQLCommand<SrcFile>(){
            public SrcFile run(Connection conn)throws Exception{
                  return SrcFile.getById(conn, srcFileId);
              }
        });
    }
    @RemoteMethod
    public void openFileInJEdit(int srcFileId){
        openRemoteJEdit(getSrcFile(srcFileId));
    }
    
    public void openRemoteJEdit(SrcFile srcFile){
        try{
            if( webResponseHandler == null)
                webResponseHandler = new WebResponseHandler();
            if(srcFile.isInZip()){
                File extracted = zipController.extractToFileIfNeed(srcFile);
                webClient.doPost("localhost:19816/openfile", "utf-8", webResponseHandler,
                    new String[]{"openfile", extracted.getPath()});
            }else{
                webClient.doPost("localhost:19816/openfile", "utf-8", webResponseHandler,
                    new String[]{"openfile", srcFile.fullpath()});
            }
        }catch(MalformedURLException e){
            throw new IronException("", e);
        }catch(DownloadException e){
            throw new IronException("", e);
        }catch(IOException e){
            throw new IronException("", e);
        }
    }
    
    private class WebResponseHandler implements ResponseStreamHandler2{
        
        public void handleStream(int responseCode, InputStream in, String charset)throws Exception{
            int b = in.read();
            while(b != -1){
                b = in.read();
            }
            in.close();
        }
        
        public void handleException(Exception ex){
            log.warning(ex.getMessage());
        }
    }
}
