package org.liujing.ironsword.ctl;

import java.util.*;
import java.io.*;
import java.util.logging.*;
import java.sql.*;
import org.liujing.ironsword.dao.*;
import liujing.util.dirscan.*;
import org.liujing.ironsword.bean.*;
import org.liujing.ironsword.IronException;
import org.antlr.runtime.ANTLRStringStream;
import org.directwebremoting.annotations.*;

@RemoteProxy(scope=ScriptScope.SESSION)
public class ProjectController{
    private Logger log = Logger.getLogger(ProjectController.class.getName());
    
    @DataTransferObject
    //(convert=org.directwebremoting.convert.EnumConverter.class)
    public static enum FindField{
        PATH,
        NAME
    }
    
    public ProjectController(){
    }
    
    public PagedVO findSrcFile(final String projectName, final String fileName,
        final String suffixName, final PagingRequest page)
    {
        ListPage result = DBWorker.getInstance().execute(new SQLCommand<ListPage>(){
                public ListPage run(Connection conn)throws Exception
                {
                    ProjectDAO dao = ProjectDAO.findByName(conn, projectName);
                    
                    return dao.findFilesByName(conn, fileName, 
                        ProjectDAO.FIND_NAME_CONTAIN, suffixName, page);
                }
            });
        return foundSrcFile2VO(result);
    }
    
    /**  findSrcFile
     @param projectId projectId
     @param fileName fileName     
     @param suffixName if value is empty or '*', it will be ignore
     @param findOpt findOpt
     @param page page
     @return PagedVO
    */
    
    public PagedVO findSrcFile(final int projectId, final String fileName,
        final String suffixName, final int findOpt, final PagingRequest page)
    {
        return foundSrcFile2VO(findSrcFilePlain(projectId, fileName, suffixName, findOpt, page));
    }
    
    @RemoteMethod
    public ListPage findSrcFilePlain(final int projectId, final String fileName,
        final String suffixName, final int findOpt, final PagingRequest page)
    {
        ListPage result = DBWorker.getInstance().execute(new SQLCommand<ListPage>(){
                public ListPage run(Connection conn)throws Exception
                {
                    ProjectDAO dao = ProjectDAO.getById(conn, projectId);
                    
                    return dao.findFilesByName(conn, fileName, 
                        findOpt, suffixName, page);
                }
            });
        return result;
    }
    
    public PagedVO findFileTree(final String projectName, final String fileName,
        final PagingRequest page){
        ListPage result = DBWorker.getInstance().execute(new SQLCommand<ListPage>(){
                public ListPage run(Connection conn)throws Exception
                {
                    ProjectDAO dao = ProjectDAO.findByName(conn, projectName);
                    return dao.findFileTreeByName(conn, fileName,
                        ProjectDAO.FIND_NAME_CONTAIN, page);
                }
            });
        return foundFileTree2VO(result);
    }
    
    public PagedVO findFileTree(final int projectId, final String fileName,
        final int findOpt, final FindField field, final PagingRequest page){
        ListPage result = DBWorker.getInstance().execute(new SQLCommand<ListPage>(){
                public ListPage run(Connection conn)throws Exception
                {
                    ProjectDAO dao = ProjectDAO.getById(conn, projectId);
                    if(field == FindField.NAME)
                        return dao.findFileTreeByName(conn, fileName,
                            findOpt, page);
                    else if(field == FindField.PATH)
                        return dao.findFileTreeByPath(conn, fileName,
                            findOpt, page);
                    return null;
                }
            });
        return foundFileTree2VO(result);
    }
    
    protected PagedVO foundSrcFile2VO(ListPage listpage){
        TableStyleVO rootTable = new TableStyleVO();
        PagedVO vo = new PagedVO(listpage, rootTable);
        TableStyleVO srcFileTable = null;
        TableStyleVO fileTreeTable = null;
        long fileTreeId = -1;
        long rootFolderId = -1;
        for(Object row : listpage.getListData()){
            Object[] cells = (Object [])row;
            String name = (String)cells[0];
            String path = (String)cells[1];
            String rootPath = (String)cells[2];
            
            long newTreeId = ((Number)cells[3]).longValue();
            long newRootId = ((Number)cells[4]).longValue();
            long srcfileId = ((Number)cells[5]).longValue();
            if(rootFolderId != newRootId){
                rootFolderId = newRootId;
                fileTreeTable = new TableStyleVO();
                rootTable.addRow(fileTreeTable, "r", rootPath, rootFolderId);
            }
            if(fileTreeId != newTreeId){
                fileTreeId = newTreeId;
                srcFileTable = new TableStyleVO();
                fileTreeTable.addRow(srcFileTable, "d", path, fileTreeId);
            }
            srcFileTable.addRow("f", name, srcfileId);
        }
        return vo;
    }
    
    protected PagedVO foundFileTree2VO(ListPage listpage){
        TableStyleVO rootTable = new TableStyleVO();
        PagedVO vo = new PagedVO(listpage, rootTable);
        TableStyleVO fileTreeTable = null;
        long rootFolderId = -1;
        for(Object row : listpage.getListData()){
            Object[] cells = (Object [])row;
            String path = (String)cells[0];
            String rootPath = (String)cells[1];
            long newRootId = ((Number)cells[3]).longValue();
            long newTreeId = ((Number)cells[2]).longValue();
            if(rootFolderId != newRootId){
                rootFolderId = newRootId;
                fileTreeTable = new TableStyleVO();
                rootTable.addRow(fileTreeTable, "r", rootPath, rootFolderId);
            }
            fileTreeTable.addRow("d", path, newTreeId);
        }
        return vo;
    }
    
    @RemoteMethod
    public DaoPagination<ProjectDAO> list(final PagingRequest page){
        return DBWorker.getInstance().execute(new SQLCommand<DaoPagination<ProjectDAO>>(){
                public DaoPagination<ProjectDAO> run(Connection conn)throws Exception
                {
                    return ProjectDAO.listProjects(conn, page);
                }
            });
    }
    
    public boolean addOrUpdateProject(final String oldName,
        final String newName, final String desc)
    {
        return DBWorker.getInstance().execute(new SQLCommand<Boolean>(){
                public Boolean run(Connection conn)throws Exception
                {
                    boolean isNew = false;
                    ProjectDAO dao = ProjectDAO.findByName(conn, oldName);
                    if(dao == null){
                        dao = new ProjectDAO();
                        isNew = true;
                    }
                    dao.setName(newName);
                    dao.setDesc(desc);
                    dao.save(conn);
                    return isNew;
                }
            });
    }
    
    @RemoteMethod
    public int addProject(final String name, final String desc){
        return DBWorker.getInstance().execute(new SQLCommand<Number>(){
                public Number run(Connection conn)throws Exception
                {
                    ProjectDAO dao = new ProjectDAO();
                    dao.setName(name);
                    dao.setDesc(desc);
                    dao.save(conn);
                    return dao.getId();
                }
            }).intValue();
    }
    
    @RemoteMethod
    public int updateProject(final ProjectDAO dao){
        return DBWorker.getInstance().execute(new SQLCommand<Number>(){
                public Number run(Connection conn)throws Exception
                {
                    dao.save(conn);
                    return dao.getId();
                }
            }).intValue();
    
    }
    
    
    public RootFolder linkFolder(final String projectName, final int folderIndex){
        return DBWorker.getInstance().execute(new SQLCommand<RootFolder>(){
                public RootFolder run(Connection conn)throws Exception
                {
                    RootFolder folder = RootFolder.findByIndex(conn, folderIndex);
                    ProjectDAO dao = ProjectDAO.findByName(conn, projectName);
                    dao.linkFolder(conn, folder);
                    return folder;
                }
            });
    }
    
    public RootFolder linkFolder(final int projectId, final int folderId){
        return DBWorker.getInstance().execute(new SQLCommand<RootFolder>(){
                public RootFolder run(Connection conn)throws Exception
                {
                    RootFolder folder = RootFolder.getById(conn, folderId);
                    ProjectDAO dao = ProjectDAO.getById(conn, projectId);
                    dao.linkFolder(conn, folder);
                    return folder;
                }
            });
    }
    
    public RootFolder unlinkFolder(final String projectName, final int folderIdxInProject){
        return DBWorker.getInstance().execute(new SQLCommand<RootFolder>(){
                public RootFolder run(Connection conn)throws Exception
                {
                    ProjectDAO dao = ProjectDAO.findByName(conn, projectName);
                    RootFolder folder = dao.getFolderByIndex(conn, folderIdxInProject);
                    dao.unlinkFolder(conn, folder);
                    return folder;
                }
            });
    }
    
    public RootFolder unlinkFolder(final int projectId, final int folderId){
        return DBWorker.getInstance().execute(new SQLCommand<RootFolder>(){
                public RootFolder run(Connection conn)throws Exception
                {
                    ProjectDAO dao = ProjectDAO.getById(conn, projectId);
                    RootFolder folder = RootFolder.getById(conn, folderId);
                    dao.unlinkFolder(conn, folder);
                    return folder;
                }
            });
    }
    
    public DaoPagination<RootFolder> listFolders(final String projectName, final DaoPagination<RootFolder> page){
        return DBWorker.getInstance().execute(new SQLCommand<DaoPagination<RootFolder>>(){
                public DaoPagination<RootFolder> run(Connection conn)throws Exception
                {
                    ProjectDAO dao = ProjectDAO.findByName(conn, projectName);
                    return dao.listFolders(conn, page);
                }
            });
    }
    
    @RemoteMethod
    public DaoPagination<RootFolder> listFolders(final int id, final DaoPagination<RootFolder> page){
        return DBWorker.getInstance().execute(new SQLCommand<DaoPagination<RootFolder>>(){
                public DaoPagination<RootFolder> run(Connection conn)throws Exception
                {
                    ProjectDAO dao = ProjectDAO.getById(conn, id);
                    return dao.listFolders(conn, page);
                }
            });
    }
    
    @RemoteMethod
    public DaoPagination<RootFolder> listUnlinkedFolders(final int projectId, 
        final PagingRequest pr)
    {
        return DBWorker.getInstance().execute(new SQLCommand<DaoPagination<RootFolder>>()
            {
                public DaoPagination<RootFolder> run(Connection conn)throws Exception
                {
                    ProjectDAO eo = ProjectDAO.getById(conn, projectId);
                    return eo.listUnlikedFolders(conn, pr);
                }
            });
        
    }
    
    public SubFilesVO listFileTreeByPath(final String projectName, final SubFilesVO vo,
        final int folderIdx, final String path)
    {
        return DBWorker.getInstance().execute(new SQLCommand<SubFilesVO>(){
                public SubFilesVO run(Connection conn)throws Exception{
                    ProjectDAO dao = ProjectDAO.findByName(conn, projectName);
                    RootFolder rootFolder = dao.getFolderByIndex(conn, folderIdx);
                    if(rootFolder == null)
                        throw new IronException("Folder of specific index does not exist");
                    DaoPagination page = new DaoPagination("Sub-files", vo.getOffset(), vo.getLimit());
                    rootFolder.listFileByPath(conn, path, page);
                    vo.setSubSrcFiles(page);
                    int size = page.getData().length;
                    if(page.hasMore())
                        vo.setMore(true);
                    else{
                        int offset2 = vo.getOffset() - page.getTotal();
                        if(offset2 < 0)
                            offset2 = 0;
                        int limit2 = vo.getLimit() - size;
                        DaoPagination page2 = new DaoPagination("Sub-folders", offset2, limit2);
                        rootFolder.fetchFileTree(conn, path, true,page2);
                        vo.setSubFolders(page2);
                        vo.setMore(page2.hasMore());
                    }
                    return vo;
                }
        });
    }
    
    public SubFilesVO expandFilesFoldersByPath(final String projectName, final PagingRequest pr,
        final int folderIdx, final String path)
    {
        return DBWorker.getInstance().execute(new SQLCommand<SubFilesVO>(){
                public SubFilesVO run(Connection conn)throws Exception{
                    ProjectDAO dao = ProjectDAO.findByName(conn, projectName);
                    RootFolder rootFolder = dao.getFolderByIndex(conn, folderIdx);
                    if(rootFolder == null)
                        throw new IronException("Folder of specific index does not exist");
                    
                    return expandFilesFoldersByPath(conn, rootFolder, pr, path);
                }
        });
    }
    
    protected static SubFilesVO expandFilesFoldersByPath(Connection conn, RootFolder rootFolder,
        PagingRequest pr, String path)throws SQLException
    {
        SubFilesVO vo = new SubFilesVO(pr);
        DaoPagination page = new DaoPagination("Sub-files", vo.getOffset(), vo.getLimit());
        rootFolder.listFileByPath(conn, path, page);
        vo.setSubSrcFiles(page);
        int size = page.getData().length;
        if(page.hasMore())
            vo.setMore(true);
        else{
            int offset2 = vo.getOffset() - page.getTotal();
            if(offset2 < 0)
                offset2 = 0;
            int limit2 = vo.getLimit() - size;
            DaoPagination pr2 = new DaoPagination("Sub-folders", offset2, limit2);
            ListPage page2 = rootFolder.fetchOneLvlFileTree(
                conn, path,pr2);
            vo.setSubFolders(page2);
            vo.setMore(page2.hasMore());
        }
        return vo;
    }
    
    public void delete(final String projName){
        DBWorker.getInstance().execute(new SQLCommand<DaoPagination<RootFolder>>(){
                public DaoPagination<RootFolder> run(Connection conn)throws Exception
                {
                    ProjectDAO proj = ProjectDAO.findByName(conn, projName);
                    proj.delete(conn);
                    return null;
                }
        });
    }
    
    public int delete(final int... ids){
        return DBWorker.getInstance().execute(new SQLCommand<Integer>(){
                public Integer run(Connection conn)throws Exception
                {
                    return ProjectDAO.deleteAll(conn, ids);
                }
        });
    }
    /** since delete is reserve word for DWR, cant not be called
    so make another signature
    */
    @RemoteMethod
    public int deleteProjects(final int[] ids){
        //return 1;
        return delete(ids);
    }
   
    
    /**  takeSnapShot
     @param projectId projectId
     @return snap shot
    */
    public SnapShotDAO takeSnapShot(final int projectId, final String desc,
        final ControllerProgressMonitor monitor){
        return DBWorker.getInstance().execute(new SQLCommand<SnapShotDAO>(){
            public SnapShotDAO run(Connection conn)throws Exception
            {
                ProjectDAO proj = ProjectDAO.getById(conn, projectId);
                SnapShotDAO snap = new SnapShotDAO();
                snap.setDesc(desc);
                snap.save(conn);
                DaoPagination<RootFolder> page = new DaoPagination(0, 9999);
                int num = 0;
                while(true){
                    page = proj.listFolders(conn, page);
                    for(int i=0;i<page.getSize();i++){
                        RootFolder folder = page.getRow(i);
                        if(monitor != null)
                            monitor.stateMessage("Folder: "+ folder.getPath());
                        num += taskSnapOfFolder(conn, folder, monitor, snap);
                    }
                    if(!page.hasMore())
                        break;
                    page.prepareNextPage();
                }
                return snap;
            }
        });
    }
    
    private int taskSnapOfFolder(Connection conn, RootFolder rf,
        ControllerProgressMonitor monitor, SnapShotDAO snap)throws SQLException
    {
        ListPage<SrcFile> page = new ListPage(0, 50);
        
        int num = 0;
        while(true){
            page = rf.listSrcFile(conn, page);
            if(monitor != null){
                 monitor.state(num);
                 if(num == 0)
                     monitor.stateMessage("total record: "+ page.getTotal());
            }
            for(int i=0,l=page.getSize();i<l;i++){
                SrcFile srcFile = page.getRow(i);
                
                SnapShotFileDAO shotItem = new SnapShotFileDAO();
                shotItem.setFullPath(srcFile.getFileTree().getPath() + "/"+ srcFile.getName());
                shotItem.setSrcFile(srcFile);
                shotItem.setSnapShot(snap);
                shotItem.setRootFolderId(srcFile.getFileTree().getRootFolder().getId());
                shotItem.save(conn);
                num++;
            }
            
            if(!page.hasMore())
                break;
            page.prepareNextPage();
        }
        return num;
    }
    
    public static DaoPagination<SnapShotDAO> listSnapShot(final PagingRequest pr){
        return DBWorker.getInstance().execute(new SQLCommand<DaoPagination<SnapShotDAO>>(){
            public DaoPagination<SnapShotDAO> run(Connection conn)throws Exception
            {
                return SnapShotDAO.listAll(conn, pr);
            }
        });
    }
    
    public void generatePatchZip(final int projectId, File targetZipFile, final int snapShotId)
    {
        
    }
    
    public void deleteSnapShot(final int snapShotId){
        
    }
}
