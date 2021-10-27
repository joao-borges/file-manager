package br.com.joaoborges.filemanager.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.commons.lang3.SystemUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import br.com.joaoborges.filemanager.operations.common.SpringUtils;
import br.com.joaoborges.filemanager.ui.listener.AboutListener;
import br.com.joaoborges.filemanager.ui.listener.ExitListener;
import br.com.joaoborges.filemanager.ui.listener.OperationExecuteListener;
import br.com.joaoborges.filemanager.ui.utils.FileInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static br.com.joaoborges.filemanager.operations.common.OperationConstants.EXTRACTION_OPERATION;
import static br.com.joaoborges.filemanager.operations.common.OperationConstants.ORGANIZATION_OPERATION;
import static br.com.joaoborges.filemanager.operations.common.OperationConstants.RENAME_OPERATION;

/**
 * Tela principal com as opcoes basicas do sistema.
 *
 * @author Joao
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class TelaPrincipal implements SmartLifecycle {

    private JFrame frame;
    private JMenuBar menu;
    private JTable table;
    private JScrollPane scrollPane;
    private final String[] columnDefs = new String[] { "Nome do arquivo", "Nome original", "Tamanho" };
    private JLabel currentDirLabel;

    private final OperationExecuteListener listener;

    @PostConstruct
    public void build() {
        log.info("Headless should be false: {}", SystemUtils.isJavaAwtHeadless());

        this.frame = new JFrame("Gerenciador de Arquivos");
        this.montarTela();

        this.frame.setSize(1280, 800);
        this.frame.setLocation(100, 100);
        this.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        this.frame.setJMenuBar(this.menu);
        this.frame.setResizable(false);
    }

    private void montarTela() {
        this.menu = new JMenuBar();

        JMenu menuPrincipal = new JMenu("Operações");
        JMenuItem renomear = new JMenuItem("Renomear arquivos");
        renomear.setActionCommand(RENAME_OPERATION);
        JMenuItem organizar = new JMenuItem("Organizar arquivos");
        organizar.setActionCommand(ORGANIZATION_OPERATION);
        JMenuItem extrair = new JMenuItem("Extrair arquivos");
        extrair.setActionCommand(EXTRACTION_OPERATION);
        JMenuItem sobre = new JMenuItem("Sobre");
        JMenuItem sair = new JMenuItem("Sair");

        renomear.addActionListener(listener);
        organizar.addActionListener(listener);
        extrair.addActionListener(listener);
        sobre.addActionListener(new AboutListener(this));
        sair.addActionListener(new ExitListener(this));

        menuPrincipal.add(renomear);
        menuPrincipal.add(organizar);
        menuPrincipal.add(extrair);
        menuPrincipal.add(sobre);
        menuPrincipal.add(sair);
        this.menu.add(menuPrincipal);

        JPanel contentPane = new JPanel(new BorderLayout(2, 2));
        this.currentDirLabel = new JLabel("", new ImageIcon(new byte[1024 * 20]), SwingConstants.CENTER);
        this.currentDirLabel.setPreferredSize(new Dimension(1270, 20));

        this.currentDirLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        contentPane.add(this.currentDirLabel, BorderLayout.NORTH);

        this.table = new JTable(new String[0][0], this.columnDefs);

        // desligo o auto resize porque senao nao consigo redimensionar
        this.table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JTableHeader header = this.table.getTableHeader();
        TableColumnModel columnModel = header.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(589);
        columnModel.getColumn(1).setPreferredWidth(589);
        columnModel.getColumn(2).setPreferredWidth(93);
        header.setColumnModel(columnModel);
        this.table.setTableHeader(header);
        this.scrollPane = new JScrollPane(this.table);

        contentPane.add(this.scrollPane, BorderLayout.CENTER);
        JPanel statusBar = new JPanel(new GridLayout(1, 5));
        JLabel label = new JLabel("Usuário: " + System.getProperty("user.name"), new ImageIcon(new byte[1024 * 20]),
                                  SwingConstants.CENTER);
        statusBar.add(label);
        statusBar.add(new JLabel());
        label = new JLabel("Sistema: " + System.getProperty("os.name"));
        label.setHorizontalTextPosition(SwingConstants.CENTER);
        statusBar.add(label);
        statusBar.add(new JLabel());
        JLabel clockLabel = new JLabel();
        clockLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        statusBar.add(clockLabel);
        ClockManager.start(clockLabel);
        contentPane.add(statusBar, BorderLayout.SOUTH);

        this.frame.setContentPane(contentPane);
    }

    public JFrame getFrame() {
        return this.frame;
    }

    public void setTopLabel(String label) {
        this.currentDirLabel.setText(label);
    }

    public void setListaArquivos(List<FileInfo> files) {
        // refaco a table com os dados da operacao
        String[][] tableData = new String[files.size()][3];
        for (int i = 0; i < files.size(); i++) {
            FileInfo info = files.get(i);
            tableData[i][0] = info.getNewName();
            tableData[i][1] = info.getOriginalName();
            tableData[i][2] = info.getSize();
        }
        createAndSetTable(tableData);
    }

    private void createAndSetTable(String[][] tableData) {
        this.table = new JTable(tableData, this.columnDefs);
        // desligo o auto resize porque senao nao consigo redimensionar
        this.table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        TableColumn tc = this.table.getColumn(this.columnDefs[0]);
        tc.setResizable(true);
        tc.setPreferredWidth(589);
        TableColumn tc1 = this.table.getColumn(this.columnDefs[1]);
        tc1.setResizable(true);
        tc1.setPreferredWidth(589);
        TableColumn tc2 = this.table.getColumn(this.columnDefs[2]);
        tc2.setResizable(true);
        tc2.setPreferredWidth(93);

        this.scrollPane.setViewportView(this.table);
        this.table.doLayout();
        this.frame.repaint();
    }

    public void start() {
        this.frame.setVisible(true);
    }

    @Override
    public void stop() {
    }

    @Override
    public boolean isRunning() {
        return this.frame != null && this.frame.isVisible();
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }
}
