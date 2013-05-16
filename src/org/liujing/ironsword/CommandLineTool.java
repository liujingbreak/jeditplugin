package org.liujing.ironsword;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import org.antlr.runtime.*;
import org.liujing.parser.*;
import liujing.jedit.parser.*;
import org.liujing.ironsword.grammar.*;
import org.liujing.ironsword.lang.*;
import org.liujing.jedit.parser.*;
import org.liujing.ironsword.dao.*;
import org.liujing.ironsword.ctl.*;
import org.liujing.ironsword.bean.*;
import org.liujing.ironsword.cmd.*;
import org.liujing.ironsword.servlet.JSGlobalSetupServlet;

public class CommandLineTool{
    private static Logger log = Logger.getLogger(CommandLineTool.class.getName());
    static Console console;
    static Map<String, CommandAction> actions = new HashMap();
    private static JavaModelBuilder javaBuilder = new JavaModelBuilder();
    protected static Map<String, String> cmdProp = new HashMap();
    private static int PAGE_SIZE = 25;
    static CommandAction[] defaultActions = new CommandAction[]{
        new CommandAction(), new ViewAction(), new TreeAction(), new PropAction(),
        new DirAction(), new ScanAction(), new AddAction(), new DeleteAction(), new TestAction()
        ,new UpdateAction(), new ProjectAction(), new WebServerAction(), new DialogCommandAction(),
        new ProxyServerAction(), new RefreshURLAction()
    };
    
    static FileScanController scanCtl = new FileScanController("default");
    static ProjectController projectCtl = new ProjectController();
    static String projectName;
    
    public static void main(String[] args)throws Exception{
        console = System.console();
        console.printf("");
        InputStream logoInput = CommandLineTool.class.getResourceAsStream("/cmd-logo.txt");
        int chr = logoInput.read();
        while(chr != -1){
            System.out.print((char)chr);
            chr = logoInput.read();
        }
        console.printf("\t* Welcome to use command line console client *\n");
        
        for(CommandAction a: defaultActions){
            actions.put(a.name(), a);
        }
        //try{
            //ServerController.startWebServer();
        //}catch(Exception e){
        //    log.log(Level.SEVERE, "Failed to start web server", e);
        //}
        while(true){
            console.printf(" >");
            String cmd = console.readLine().trim();
            if(cmd.equals("quit") || cmd.equals("exit")){
                console.printf("bye bye!");
                break;
            }
            onCommand(cmd);
        }
        ServerController.stopWebServer();
        ProxyServerController.stopServer();
        DBWorker.closeAll();
    }
    
    private static void onCommand(String cmd){
        try{
            ConsoleCommandModel cmdModel = splitCmd(cmd);
            if(cmdModel.getKeywords().size() <= 0)
                return;
            String actionName = cmdModel.getKeywords().removeFirst();
            CommandAction action = null;
            action = actions.get(actionName);
            if(action == null){
                console.printf("Unknow command: %1$s\n", cmd);
                return;
            }
            //String[] a = new String[args.length -1];
            //System.arraycopy(args, 1, a, 0, args.length -1 );
            
            String[] a = cmdModel.getKeywords().toArray(new String[0]);
            if(action instanceof AdvancedCMDAction){
                ((AdvancedCMDAction)action).setCmdContext(cmdModel);
            }
            action.action(console, a);
            
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
    
    
    public static ConsoleCommandModel splitCmd(String cmd)throws RecognitionException{
        try{
            ANTLRStringStream in = new ANTLRStringStream(cmd);
            CommandLineLexer lexer = new CommandLineLexer(in);
            CommandLineParser parser = new CommandLineParser(
                new RemovableTokenStream(lexer) );
            AntlrGrammarHandler handler = new AntlrGrammarHandler();
            parser.setHandler(handler);
            parser.input();
            //console.printf("%1$s\n", handler.getNode().toString());
            if(log.isLoggable(Level.FINE))
                log.fine(handler.getNode().toString());
            ConsoleCommandModel cmdModel = new ConsoleCommandModel(handler.getNode());
            return cmdModel;
        }catch(RecognitionException re){
            //re.printStackTrace();
            throw re;
        }
    }
    
    
    public static boolean nextPage(Console con, DaoPagination p){
        if(p.hasMore()){
            String line = con.readLine("Continue... (Press x to stop display) ");
            if(!line.equalsIgnoreCase("x")){
                    p.prepareNextPage();
                    return true;
            }
        }
        return false;
    }
    
    public static void pagingPrint(Console con, CmdLinePagingHandler handler){
        PagingRequest pr = new PagingRequest(0, PAGE_SIZE);
        while(true){
            DaoPagination page = handler.fetchPage(pr);
            con.printf(page.toString());
            con.printf("\n");
            if(!page.hasMore())
                break;
            String line = con.readLine("Continue... (Press x to stop display) ");
            if(!line.equalsIgnoreCase("x")){
                pr = page.prepareNextPage();
            }else{
                break;
            }
        }
    }
    
    static class CommandAction{
        
        public CommandAction(){}
        
        public String name(){
            return "help";
        }
        
        public String help(){
            return null;
        }
        
        public void action(Console con, String[] args)throws Exception{
            if(args.length == 0){
                con.printf("available command list:\n");
                for(CommandAction action : actions.values()){
                    con.printf(action.name() + "\n");
                }
            }else if(args.length == 1){
                CommandAction a = actions.get(args[0]);
                if(a == null){
                    con.printf("Command not found\n");
                    return;
                }
                if(a.help() != null)
                    con.printf("%1$s\n", a.help());
                else
                    con.printf("No detail help information\n");
            }
        }
    }
    
    public static class AdvancedCMDAction extends CommandAction{
        protected ConsoleCommandModel context;
        
        public AdvancedCMDAction(){
        }
        
        public void setCmdContext(ConsoleCommandModel cmdModel){
            this.context = cmdModel;
        }
    }
    
    /**
    set properties
    */
    static class PropAction extends CommandAction{
        public String name(){
            return "prop";
        }
        
        public void action(Console con, String[] args) throws Exception{
            if(args.length >= 2){
                if("pagesize".equals(args[0])){
                    PAGE_SIZE = Integer.parseInt(args[1]);
                    con.printf("page size reset to %1$d\n", PAGE_SIZE);
                    return;
                }
                cmdProp.put(args[0], args[1]);
                con.printf("Property set, size %1$d\n", cmdProp.size());
            }else {
                con.printf("Properties:\n");
                for(Map.Entry<String, String> en: cmdProp.entrySet()){
                    con.printf("\t%1$s = %2$s\n", en.getKey(), en.getValue());
                }
            }
        }
    }
    
    static class TreeAction extends CommandAction{
        public String name(){
            return "tree";
        }
        
        public void action(Console con, String[] args)throws Exception{
            if(args[0].length()>5 && args[0].substring(args[0].length() - 5).equals(".java")){
                AntlrGrammarHandler h = new AntlrGrammarHandler();
                //ANTLRFileStream in = new ANTLRFileStream(args[0]);
                JavaLexer lexer = new JavaLexer(new ANTLRFileStream(args[0]));
                JavaParser p = new JavaParser(new RemovableTokenStream(lexer));
                p.setAntlrGrammarHandler(h);
                p.compilationUnit(null);
                h.setName(args[0]);
                //h.printTree();
                con.printf("%1$s\n", h.getNode());
            }else if(args[0].length()>3 && args[0].substring(args[0].length() - 3).equals(".py")){
                AntlrGrammarHandler h = new AntlrGrammarHandler();
                PythonLexer lexer = new PythonLexer(new ANTLRFileStream(args[0]));
                PythonParser p = new PythonParser(new RemovableTokenStream(lexer));
                p.setHandler(h);
                p.file_input();
                h.setName(args[0]);
                con.printf("%1$s\n", h.getNode());
            }else if(args[0].length()>4 && args[0].substring(args[0].length() - 4).equals(".css")){
                AntlrGrammarHandler h = new AntlrGrammarHandler();
                MyCSSLexer lexer = new MyCSSLexer(new ANTLRFileStream(args[0]));
                MyCSSParser p = new MyCSSParser(new RemovableTokenStream(lexer));
                p.setHandler(h);
                p.cssfile(args[0]);
                h.setName(args[0]);
                con.printf("%1$s\n", h.getNode());
            }
            
        }
    }
    
    static class ViewAction extends CommandAction{
        public String name(){
            return "view";
        }
        
        public void action(Console con, String[] args){
            if(args[0].length()>5 && args[0].substring(args[0].length() - 5).equals(".java")){
                CommonLanguageModel m = javaBuilder.build(new File(args[0]));
                con.printf(m.toString());
            }
        }
    }
    
    public static class AddAction extends CommandAction{
        public String name(){
            return "add";
        }
        
        public String help(){
            return "add root folder\nadd <Path> [include pattern] [exclude pattern]";
        }
        
        public void action(Console con, String[] args){
            String path = "./**/*";
            if(args.length > 0)
              path = args[0];
            
            String include = null;
            String exclude = null;
            if(args.length >= 2)
                include = args[1];
            if(args.length >= 3)
                exclude = args[2];
            int c = scanCtl.addRootFolder(args[0], include, exclude);
            con.printf("number of files: %1$s\n", c);
        }
    }
    
    public static class UpdateAction extends CommandAction{
        public String name(){
            return "update";
        }
        
        public String help(){
            return "update root folder\nupdate <Path> [include pattern] [exclude pattern]";
        }
        
        public void action(Console con, String[] args){
            String path = "./**/*";
            if(args.length > 0)
              path = args[0];
            
            String include = null;
            String exclude = null;
            if(args.length >= 2)
                include = args[1];
            if(args.length >= 3)
                exclude = args[2];
            boolean newly = scanCtl.updateRootFolder(args[0], include, exclude);
            if(newly)
              con.printf("Not found, create a new one\n");
        }
    }
    
    static class ScanAction extends CommandAction{
        public String name(){
            return "scan";
        }
        
        public String help(){
            return "scan [root folder Index] [path]";
        }
        
        public void action(Console con, String[] args){
            int index = 0;
            String coverPath = null;
            
            if(args.length >= 2){
                coverPath = args[1];
                index = Integer.parseInt(args[0]) -1;
            }else if(args.length >= 1){
                index = Integer.parseInt(args[0]) -1;
                coverPath = "";
            }else{
                con.printf("%1$s\n", help());
                return;
            }
            ScanResultVO vo = scanCtl.rescan(index, coverPath);
            con.printf("Scan done, %1$s\n", vo);
        }
    }
    
    static class DirAction extends AdvancedCMDAction{
        public String name(){
            return "dir";
        }
        
        public String help(){
            return "dir [-r] [root folder Index] [directory]"+ 
                   "    -r recursively list all sub folders";
        }
        
        public void action(Console con, final String[] args)throws Exception{
            DaoPagination page = new DaoPagination(0, PAGE_SIZE);
            if(args.length == 0){
                do{
                    scanCtl.dirRootFolders(page);
                    con.printf("%1$s\n%2$s\n", page.toString(), page.hasMore()?" - More ... -":"- End -");
                }while(nextPage(con, page));
                
            }else if(args.length == 1){
                int rootIdx = 0;
                String path = "";
                try{
                    rootIdx = Integer.parseInt(args[0]) - 1;
                }catch(NumberFormatException ne){
                   String sRoot = cmdProp.get("root");
                   if(sRoot != null){
                       rootIdx = Integer.parseInt(sRoot) - 1;
                   }
                   path = args[0];
                }
                final int f_rootIdx = rootIdx;
                final String f_path = path;
                if(context.getOption("r") == null){
                    pagingPrint(con, new CmdLinePagingHandler(){
                            public DaoPagination fetchPage(PagingRequest pr){
                                return scanCtl.expandFilesFoldersByPath(f_rootIdx, f_path, pr);
                        }
                    });
                }else{
                    pagingPrint(con, new CmdLinePagingHandler(){
                            public DaoPagination fetchPage(PagingRequest pr){
                                return scanCtl.dir(f_rootIdx, f_path, pr.getOffset(), pr.getLimit());
                            }
                    });
                }
            }else if(args.length == 2){
                if(context.getOption("r") == null){
                    pagingPrint(con, new CmdLinePagingHandler(){
                            public DaoPagination fetchPage(PagingRequest pr){
                                return scanCtl.expandFilesFoldersByPath(Integer.parseInt(args[0]) -1, args[1], pr);
                        }
                    });
                }else{
                    pagingPrint(con, new CmdLinePagingHandler(){
                            public DaoPagination fetchPage(PagingRequest pr){
                                return scanCtl.dir(Integer.parseInt(args[0]) -1, args[1], pr.getOffset(), pr.getLimit());
                            }
                    });
                }
            }
        }
    }
    
    static class DeleteAction extends CommandAction{
        public String name(){
            return "del";
        }
        
        public String help(){
            return "del <root folder index>";
        }
        
        public void action(Console con, String[] args)throws Exception{
            int r = scanCtl.deleteRootFolder(Integer.parseInt(args[0]));
            con.printf("%1$d deleted\n", r);
        }
    }
    
    static class ProjectAction extends AdvancedCMDAction{
        public String name(){
            return "project";
        }
        
        public String help(){
            StringBuilder sb = new StringBuilder();
            sb.append("List projects:                project [list]\n" )
            .append("Update or create project:     project update [-name=<older name>] [-new=<new name>] -desc=<new description>\n" )
            .append("Switch to & view project:     project view <project name> [root folder index] [-r] [path]\n")
            .append("                                -r recursively list all sub folders\n")
            .append("delete a project:             project delete <project name>\n" )
            .append("link folder to a project:     project link [<project name>] <folder index>\n")
            .append("unlink folder from a project: project unlink [<project name>] <folder index>\n")
            .append("find file from a project:     project find [-f|-d] [<project name>] [-t=<type>] <file name>\n")
            .append("                                   -f find file\n")
            .append("                                   -d find directory\n")
            .append("                                   -t file type/suffix, e.g. java\n");
            return sb.toString();
        }
        
        public void action(Console con, String[] args)throws Exception{
            if(!context.itr().hasNext()){
                list(con);
                return;
            }
            String oper = context.itr().next();
            if("list".equals(oper)){
                list(con);
            }else if("update".equals(oper)){
                update(con);
            }else if("delete".equals(oper)){
                delete(con);
            }else if("link".equals(oper)){
                linkFolder(con);
            }else if("unlink".equals(oper)){
                unlinkFolder(con);
            }else if("find".equals(oper)){
                find(con);
            }else if("view".equals(oper)){
                view(con);
            }
        }
        private void update(Console con){
            String name = context.getOption("name"),
            newName = context.getOption("new"),
            desc = context.getOption("desc");
            
            if(name == null) name = newName;
            if(newName == null) newName = name;
            
            if(projectCtl.addOrUpdateProject(name, newName, desc))
                con.printf("new project created\n");
            else
                con.printf("project udpated\n");
        }
        
        private void list(Console con){
            DaoPagination<ProjectDAO> page = new DaoPagination(0, PAGE_SIZE);
            do{
                projectCtl.list(page);
                con.printf("%1$s\n", page.toString());
            }while(nextPage(con, page));
        }
            
        private void view(Console con){
            projectName = context.itr().next();
            if(context.itr().hasNext()){
                
                final int folderIdx = Integer.parseInt(context.itr().next()) -1;
                SubFilesVO page = new SubFilesVO(0, PAGE_SIZE);
                final String path = context.itr().hasNext()?context.itr().next():null;
                if(context.getOption("r") != null){
                    do{
                        projectCtl.listFileTreeByPath(projectName, page, folderIdx, path);
                        con.printf("%1$s\n", page.toString());
                    }while(nextPage(con, page));
                }else{
                    pagingPrint(con, new CmdLinePagingHandler(){
                            public DaoPagination fetchPage(PagingRequest pr){
                                return projectCtl.expandFilesFoldersByPath(projectName, pr, folderIdx, path);
                            }
                    });
                }
            }else{
                DaoPagination<RootFolder> page = new DaoPagination(0, PAGE_SIZE);
                do{
                    projectCtl.listFolders(projectName, page);
                    con.printf("%1$s\n", page.toString());
                }while(nextPage(con, page));
            }
        }
        private void delete(Console con){
            projectName = context.itr().next();
            projectCtl.delete(projectName);
            con.printf("deleted\n");
        }
        private void linkFolder(Console con){
            int folder = 0;
            if(context.getKeywords().size() == 3){
                projectName = context.itr().next();
            }
            folder = Integer.parseInt(context.itr().next()) -1;
            RootFolder rf = projectCtl.linkFolder(projectName, folder);
            con.printf("add folder: %1$s\n to Project %2$s\n", rf,projectName);
        }
        private void unlinkFolder(Console con){
            int folder = 0;
            if(context.getKeywords().size() == 3){
                projectName = context.itr().next();
            }
            folder = Integer.parseInt(context.itr().next()) -1;
            RootFolder rf = projectCtl.unlinkFolder(projectName, folder);
            con.printf("remove folder: %1$s\n from Project:%2$s\n", rf, projectName);
        }
        private void find(Console con){
            int folder = 0;
            if(context.getKeywords().size() == 3){
                projectName = context.itr().next();
            }
            final String findKeyword = context.itr().next();
            if(context.getOption("d")!= null){
                pagingPrint(con, new CmdLinePagingHandler(){
                     public DaoPagination fetchPage(PagingRequest pr){
                         //return projectCtl.findFileTree(projectName, findKeyword,
                         //    context.getOption("t"), pr);
			return null;
                     }
                });
            }else{
                pagingPrint(con, new CmdLinePagingHandler(){
                     public DaoPagination fetchPage(PagingRequest pr){
                         return projectCtl.findSrcFile(projectName, findKeyword,
                             context.getOption("t"), pr);
                     }
                });
            }
        }
    }
    
    static class TestAction extends CommandAction{
        public String name(){
            return "test";
        }
        
        public void action(Console con, String[] args)throws Exception{
            SQLScriptLexer lex = new SQLScriptLexer(new ANTLRFileStream(args[0]));
            SQLScriptParser par = new SQLScriptParser(new RemovableTokenStream(lex));
            par.setSQLHandler(new PrintSQLHandler(con));
            par.script();
            
        }
    }
    
    static class WebServerAction extends CommandAction{
        public String name(){
            return "server";
        }
        
        public void action(Console con, String[] args)throws Exception{
            ServerController.startWebServer();            
        }
    }
    
    public static class PrintSQLHandler implements DBWorker.SQLHandler{
        public Console con;
        public PrintSQLHandler(Console c){
            this.con = c;
        }
        public void onSQL(String sql){
          con.printf("%1$s\n", sql);
        }
      }
    
    public static class ProxyServerAction extends CommandAction{
        public String name(){
            return "proxy";
        }
        
        public String help(){
            return "proxy start|stop";
        }
        
        public void action(Console con, String[] args)throws Exception{
            if(args.length > 0){
                if(args[0].equalsIgnoreCase("start")){
                    ProxyServerController.startServer();
                }else if(args[0].equalsIgnoreCase("stop")){
                    ProxyServerController.stopServer();
                }else if(args[0].equalsIgnoreCase("test")){
                    ProxyServerController.testRequest();
                }
            }
                        
        }
    }
    public static class RefreshURLAction extends CommandAction{
        public String name(){
            return "refreshurl";
        }
        public String help(){
            return "refreshurl\t- refresh jetty's URL token";
        }
        public void action(Console con, String[] args)throws Exception{
            JSGlobalSetupServlet.refreshURLToken();
            ServerController.stopWebServer();
            ServerController.startWebServer();
        }
    }
}
