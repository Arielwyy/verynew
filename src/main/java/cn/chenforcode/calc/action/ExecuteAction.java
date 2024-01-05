package cn.chenforcode.calc.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindowManager;
import okhttp3.*;

import javax.swing.*;
import java.io.IOException;

// "execute" 菜单项的动作处理器
public class ExecuteAction extends AnAction {

    // 网络请求客户端: 使用OkHttp库
    private final OkHttpClient httpClient = new OkHttpClient();

    @Override
    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getProject();
        if (project == null) return;

        final Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor == null) return;

        Document document = editor.getDocument();
        String content = document.getText();

        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            Request request = new Request.Builder()
                    .url("http://api.chenforcode.cn")
                    .post(RequestBody.create(MediaType.parse("text/plain"), content))
                    .build();
            try {
                Response response = httpClient.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    displayResultInToolWindow(responseBody, project);
                } else {
// 处理错误或无响应的情况
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });
    }

    // 在IDEA侧边栏显示结果
    private void displayResultInToolWindow(String result, Project project) {
        SwingUtilities.invokeLater(() -> {
            // 获取 or 创建工具窗口
            ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
            ToolWindow myToolWindow = toolWindowManager.getToolWindow("MyToolWindow");
            if (myToolWindow == null) {
                // 工具窗口不存在时，需要创建和注册它
            }

            // 展示结果到侧边栏
            Content content = myToolWindow.getContentManager().getContent(0);
            if (content != null) {
                JComponent component = content.getComponent();
                if (component instanceof MyCustomPanel) {
                    ((MyCustomPanel) component).updateWithResponse(result);
                }
            }
            myToolWindow.show(null);// 激活工具窗口
        });
    }

    // 自定义面板类
    public static class MyCustomPanel extends JPanel {
        private JTextArea textArea;

        public MyCustomPanel() {
            textArea = new JTextArea(20, 50);
            add(new JScrollPane(textArea));// 添加滚动条
        }

        public void updateWithResponse(String response) {
            textArea.setText(response);
        }
    }
}

