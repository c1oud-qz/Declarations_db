package main;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import connection.DatabaseConnection;


public class DB_Work extends JFrame implements ActionListener{
    private javax.swing.JTable table;
    private JButton saveButton = new JButton("Выполнить");
    private JButton discardButton = new JButton("Закрыть");
    private JMenuItem dtItem;
    private JMenuItem riskProfItem;
    private JMenuItem tstkItem;
    private JMenuItem importMenuItem;
    private JMenuItem exportMenuItem;

    public DB_Work() {
        super("Приложение для работы с базой данных 'Декларации'");
        init();
        // Устанавливаем параметры окна
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        int h = (int)screenSize.getHeight();
        int w = (int)screenSize.getWidth();
        setSize(w, h-30);

        // Создаем компоненты GUI
        // create a menu bar
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        // create a menu
        JMenu queueMenu = new JMenu("Создать запрос");
        menuBar.add(queueMenu);

        // create menu items
        JMenuItem insertMenuItem = new JMenuItem("Добавить запись");
        JMenuItem updateMenuItem = new JMenuItem("Редактировать запись");
        JMenuItem deleteMenuItem = new JMenuItem("Удалить запись");

        queueMenu.add(insertMenuItem);
        queueMenu.add(updateMenuItem);
        queueMenu.add(deleteMenuItem);

        insertMenuItem.addActionListener(this);
        updateMenuItem.addActionListener(this);
        deleteMenuItem.addActionListener(this);

        JMenu checkDT = new JMenu("Проверить декларации");
        menuBar.add(checkDT);
        JMenuItem refreshMenuItem = new JMenuItem("Обновить базу деклараций");
        checkDT.add(refreshMenuItem);
        refreshMenuItem.addActionListener(this);
        JMenuItem checkMenuItem = new JMenuItem("Показать рисковые декларации");
        checkDT.add(checkMenuItem);
        checkMenuItem.addActionListener(this);
        JMenuItem delMenuItem = new JMenuItem("Удалить информацию о профилях рисков во всех ДТ");
        checkDT.add(delMenuItem);
        delMenuItem.addActionListener(this);

        JMenu procedureMenu = new JMenu("Выбрать процедуру");
        menuBar.add(procedureMenu);

        // create menu items
        importMenuItem = new JMenuItem("Импорт");
        exportMenuItem = new JMenuItem("Экспорт");

        procedureMenu.add(importMenuItem);
        procedureMenu.add(exportMenuItem);

        importMenuItem.addActionListener(this);
        exportMenuItem.addActionListener(this);

        // create a menu
        JMenu tablesMenu = new JMenu("Связанные таблицы");
        menuBar.add(tablesMenu);

        // create menu items
        dtItem = new JMenuItem("Декларации");
        riskProfItem = new JMenuItem("Профили рисков");
        tstkItem = new JMenuItem("Перечень ТСТК");

        tablesMenu.add(dtItem);
        tablesMenu.add(riskProfItem);
        tablesMenu.add(tstkItem);

        dtItem.addActionListener(this);
        riskProfItem.addActionListener(this);
        tstkItem.addActionListener(this);

        // Добавление таблицы
        table = new JTable();
        add(table);

        // Получаем данные из MySQL и заполняем таблицу
        loadDeclarations();

        // Размещаем компоненты на форме
        JScrollPane scrollPane = new JScrollPane(table);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
    }

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        if (command.equals("Добавить запись")) {
            insDT();
        }
        else if (command.equals("Редактировать запись")) {
            updDT();
        }
        else if (command.equals("Удалить запись")) {
            delDT();
        }
        else if (command.equals("Обновить базу деклараций")) {
            loadRiskyDT();
            init();
        }
        else if (command.equals("Показать рисковые декларации")) {
            showRiskDT();
            init();
        }
        else if(command.equals("Удалить информацию о профилях рисков во всех ДТ")){
            hidePR();
        }
        else if (command.equals("Декларации")) {
            loadDeclarations();
            dtItem.setEnabled(false);
            riskProfItem.setEnabled(true);
            tstkItem.setEnabled(true);
        }
        else if (command.equals("Профили рисков")) {
            loadProfiles();
            dtItem.setEnabled(true);
            riskProfItem.setEnabled(false);
            tstkItem.setEnabled(true);
        }
        else if (command.equals("Перечень ТСТК")) {
            loadTSTK();
            dtItem.setEnabled(true);
            riskProfItem.setEnabled(true);
            tstkItem.setEnabled(false);
        }
        else if (command.equals("Импорт")) {
            // JDialog d7 = new JDialog(DB_Work.this, "IMPORT DECLARATIONS");
            loadImport();
            importMenuItem.setEnabled(false);
            exportMenuItem.setEnabled(true);
        }
        else if (command.equals("Экспорт")) {
            // JDialog d8 = new JDialog(DB_Work.this, "EXPORT DECLARATIONS");
            loadExport();
            importMenuItem.setEnabled(true);
            exportMenuItem.setEnabled(false);
        }
         
    }

    private void init() {
        try {
            DatabaseConnection.getInstance().connectToDatabase();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadDeclarations() {
        try {
            // Создаем запрос SQL
            PreparedStatement p = DatabaseConnection.getInstance().getConnection().prepareStatement("select * from декларации");
            ResultSet resultSet = p.executeQuery();

            // Создаем модель таблицы для отображения данных
            DefaultTableModel model = new DefaultTableModel();

            // Получаем метаданные результатов запроса
            int columnCount = resultSet.getMetaData().getColumnCount();

            // Добавляем колонки в модель
            for (int i = 1; i <= columnCount; i++) {
                model.addColumn(resultSet.getMetaData().getColumnName(i));
            }

            // Добавляем строки в модель
            while (resultSet.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = resultSet.getObject(i);
                }
                model.addRow(row);
            }

            // Устанавливаем модель для таблицы
            table.setModel(model);

            // Закрываем ресурсы
            resultSet.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hidePR(){
        try{
            PreparedStatement p = DatabaseConnection.getInstance().getConnection().prepareStatement("update декларации set профиль_риска = NULL where профиль_риска is not null;");
            p.executeUpdate();
            PreparedStatement p1 = DatabaseConnection.getInstance().getConnection().prepareStatement("select * from декларации");
            ResultSet resultSet = p1.executeQuery();
            DefaultTableModel model = new DefaultTableModel();
            int columnCount = resultSet.getMetaData().getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                model.addColumn(resultSet.getMetaData().getColumnName(i));
            }
            while (resultSet.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = resultSet.getObject(i);
                }
                model.addRow(row);
            }
            table.setModel(model);
            resultSet.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadRiskyDT(){
        Connection connection = DatabaseConnection.getInstance().getConnection();
        try (connection){
            String createTableQuery = "CREATE TEMPORARY TABLE temp_results AS SELECT декларации.id_ДТ, профили_риска.№ПРполн AS профил FROM декларации INNER JOIN профили_риска ON (декларации.код_ТНВЭД = профили_риска.товар) AND (декларации.страна_проис = профили_риска.страна) AND (декларации.дата_подачи < профили_риска.датаОконч) AND LEFT(профили_риска.создавшийТО, 3)=LEFT(декларации.кодТО, 3);";
            try (PreparedStatement createTableStatement = connection.prepareStatement(createTableQuery)) {
                createTableStatement.executeUpdate();
            }

            // Запрос на выборку данных из временной таблицы
                String st1 = "START TRANSACTION;";
                String st2 = "UPDATE декларации JOIN temp_results ON декларации.id_ДТ = temp_results.id_ДТ SET декларации.профиль_риска = temp_results.профил;";
                String st3 = "SELECT * from декларации;";
                String st4 = "DROP TEMPORARY TABLE IF EXISTS temp_results;";
                
                PreparedStatement p1 = connection.prepareStatement(st1);
                PreparedStatement p2 = connection.prepareStatement(st2);
                PreparedStatement p3 = connection.prepareStatement(st3);
                PreparedStatement p4 = connection.prepareStatement(st4);

                connection.setAutoCommit(false);
                p1.executeUpdate();
                p2.executeUpdate();
  
            try (ResultSet resultSet1 = p3.executeQuery()) {

                // Обработка результатов запроса
                DefaultTableModel model1 = new DefaultTableModel();
                int columnCount1 = resultSet1.getMetaData().getColumnCount();
                for (int i = 1; i <= columnCount1; i++) {
                    model1.addColumn(resultSet1.getMetaData().getColumnName(i));
                }
                while (resultSet1.next()) {
                    Object[] row = new Object[columnCount1];
                    for (int i = 1; i <= columnCount1; i++) {
                        row[i - 1] = resultSet1.getObject(i);
                    }
                    model1.addRow(row);
                }

                // Установка модели данных для вашей таблицы
                table.setModel(model1);
                // connection.rollback();
                p4.executeUpdate();
                connection.setAutoCommit(true);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showRiskDT(){
        Connection connection = DatabaseConnection.getInstance().getConnection();
        try (connection){
            JTable resultTable = new JTable();
            String createTableQuery2 = "create temporary table temp_TO as select * from перечень_тстк inner join декларации on (перечень_тстк.код_ТО_размещ = декларации.кодТО) inner join профили_риска on (перечень_тстк.вид = профили_риска.испТСТК) where ((декларации.профиль_риска is not null) and (профили_риска.№ПРполн=декларации.профиль_риска));";
            try (PreparedStatement createTableStatement = connection.prepareStatement(createTableQuery2)) {
                int rowsAffected = createTableStatement.executeUpdate();
                if (rowsAffected==0){
                    JOptionPane.showMessageDialog(null, "Результат создания временной таблицы пустой", "Информация", JOptionPane.INFORMATION_MESSAGE);
                }
            }
            String st5 = "SELECT * from декларации WHERE профиль_риска is not null;";
            String st6 = "select №_ДТ, код_ТНВЭД, испТСТК, название, технич_номер, кодТО, допТСТК from temp_TO;";
            String st7 = "DROP TEMPORARY TABLE IF EXISTS temp_TO;";
            PreparedStatement p5 = connection.prepareStatement(st5);
            PreparedStatement p6 = connection.prepareStatement(st6);
            PreparedStatement p7 = connection.prepareStatement(st7);

            JDialog messageRisk = new JDialog(DB_Work.this, "Список ДТ с профилем риска");
            messageRisk.setSize(1200, 400);
            JButton showButton = new JButton("Применить ТСТК");
            int rows = 0;
            try {
                // Создаем запрос SQL
                ResultSet resultSet = p5.executeQuery();
                DefaultTableModel model = new DefaultTableModel();
                int columnCount = resultSet.getMetaData().getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    model.addColumn(resultSet.getMetaData().getColumnName(i));
                }
                while (resultSet.next()) {
                    rows++;
                    Object[] row = new Object[columnCount];
                    for (int i = 1; i <= columnCount; i++) {
                        row[i - 1] = resultSet.getObject(i);
                    }
                    model.addRow(row);
                }
                resultTable.setModel(model);
                resultSet.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            String spotted = "Выявлено "+rows+" рисковые(-х) декларации(-й).";
            if (rows==0){
                JOptionPane.showMessageDialog(null, "Нет рисковых деклараций!", "Информация", JOptionPane.OK_CANCEL_OPTION);
            }
            JLabel label = new JLabel(spotted);
            JPanel p1 = new JPanel();
            p1.add(label);
            p1.add(new JScrollPane(resultTable));
            p1.add(showButton);
            p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
            messageRisk.add(p1);
            // messageRisk.add(label);
            // messageRisk.add(new JScrollPane(resultTable));
            // messageRisk.add(showButton);
            // messageRisk.setLayout(new BoxLayout(messageRisk, BoxLayout.Y_AXIS));
            messageRisk.setVisible(true);
            messageRisk.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                // новое окно-уведомление
            String opportunity = "Возможно использовать следующие ТСТК при таможенном контроле:";
            JLabel lavel_opp = new JLabel(opportunity);
            JPanel panel = new JPanel();
            JTable innerTable = new JTable();
            try{
                ResultSet resultSet = p6.executeQuery();
                DefaultTableModel model = new DefaultTableModel();
                int columnCount = resultSet.getMetaData().getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    model.addColumn(resultSet.getMetaData().getColumnName(i));
                }
                while (resultSet.next()) {
                    Object[] row = new Object[columnCount];
                    for (int i = 1; i <= columnCount; i++) {
                        row[i - 1] = resultSet.getObject(i);
                    }
                    model.addRow(row);
                }
                innerTable.setModel(model);
                resultSet.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            panel.add(lavel_opp);
            panel.add(new JScrollPane(innerTable));
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            showButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e4){
                    JOptionPane.showMessageDialog(null, panel, "Используем ТСТК", JOptionPane.OK_CANCEL_OPTION);
                    messageRisk.dispose();
                }
            });

            p7.executeUpdate();
            connection.setAutoCommit(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadProfiles() {
        // открываем таблицу Профили рисков
        try {
            PreparedStatement p = DatabaseConnection.getInstance().getConnection().prepareStatement("select * from профили_риска");
            ResultSet resultSet = p.executeQuery();
            DefaultTableModel model = new DefaultTableModel();
            int columnCount = resultSet.getMetaData().getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                model.addColumn(resultSet.getMetaData().getColumnName(i));
            }
            while (resultSet.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = resultSet.getObject(i);
                }
                model.addRow(row);
            }
            table.setModel(model);
            resultSet.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void loadTSTK() {
        // открываем таблицу Профили рисков
        try {
            PreparedStatement p = DatabaseConnection.getInstance().getConnection().prepareStatement("select * from перечень_тстк");
            ResultSet resultSet = p.executeQuery();
            DefaultTableModel model = new DefaultTableModel();
            int columnCount = resultSet.getMetaData().getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                model.addColumn(resultSet.getMetaData().getColumnName(i));
            }
            while (resultSet.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = resultSet.getObject(i);
                }
                model.addRow(row);
            }
            table.setModel(model);
            resultSet.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void loadImport() {
        // срез по импорту
        try {
            PreparedStatement p = DatabaseConnection.getInstance().getConnection().prepareStatement("select * from декларации where страна_проис<>'RU'");
            ResultSet resultSet = p.executeQuery();
            DefaultTableModel model = new DefaultTableModel();
            int columnCount = resultSet.getMetaData().getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                model.addColumn(resultSet.getMetaData().getColumnName(i));
            }
            while (resultSet.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = resultSet.getObject(i);
                }
                model.addRow(row);
            }
            table.setModel(model);
            resultSet.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void loadExport() {
        // срез по экспорту
        try {
            PreparedStatement p = DatabaseConnection.getInstance().getConnection().prepareStatement("select * from декларации where страна_проис='RU'");
            ResultSet resultSet = p.executeQuery();
            DefaultTableModel model = new DefaultTableModel();
            int columnCount = resultSet.getMetaData().getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                model.addColumn(resultSet.getMetaData().getColumnName(i));
            }
            while (resultSet.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = resultSet.getObject(i);
                }
                model.addRow(row);
            }
            table.setModel(model);
            resultSet.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void insDT(){
        JDialog d1 = new JDialog(DB_Work.this, "-INSERT- SQL QUERY");
        d1.setModal(true);
        JTextArea queryArea = new JTextArea("INSERT INTO декларации (id_ДТ, №_ДТ, декларант, кодТО, дата_подачи, номер, код_ТНВЭД, там_стоимость, страна_проис, вес_нетто, вес_брутто) VALUES",1,5);
        queryArea.setLineWrap(true);
        queryArea.setWrapStyleWord(true);
        discardButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                d1.dispose();
            }
        });
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try{
                    PreparedStatement p = DatabaseConnection.getInstance().getConnection().prepareStatement(queryArea.getText());
                        p.executeUpdate();
                        JOptionPane.showMessageDialog(null, "Успешно", "Изменено", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception e1) {
                        JScrollPane scroll = new JScrollPane(new JLabel("Произошла ошибка: " + e.toString()));
                        JOptionPane optionPane = new JOptionPane(scroll, JOptionPane.ERROR_MESSAGE);
                        optionPane.setMaximumSize(new Dimension(400, 300));
                        JDialog dialog = optionPane.createDialog("Ошибка");
                        dialog.setVisible(true);
                    }
                }
            });
        JPanel full = new JPanel();
        JPanel gr = new JPanel();
        gr.setPreferredSize(new Dimension(600, 70));
        gr.add(discardButton);
        gr.add(saveButton);
        
        gr.setLayout(new BoxLayout(gr, BoxLayout.X_AXIS));
        // text.add(queryArea);
        full.add(queryArea);
        full.add(gr);
        full.setLayout(new BoxLayout(full,BoxLayout.Y_AXIS));
        d1.add(full);
        d1.setSize(600,300);
        d1.setVisible(true);
    }

    private void updDT(){
        JDialog d2 = new JDialog(DB_Work.this, "-UPDATE- SQL QUERY");
        JTextArea queryArea = new JTextArea("UPDATE декларации SET");
        d2.setModal(true);
        discardButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                d2.dispose();
            }
        });
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try{
                    PreparedStatement p = DatabaseConnection.getInstance().getConnection().prepareStatement(queryArea.getText());
                        p.executeUpdate();
                        JOptionPane.showMessageDialog(null, "Успешно", "Изменено", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception e1) {
                        JScrollPane scroll = new JScrollPane(new JLabel("Произошла ошибка: " + e.toString()));
                        JOptionPane optionPane = new JOptionPane(scroll, JOptionPane.ERROR_MESSAGE);
                        optionPane.setMaximumSize(new Dimension(400, 300));
                        JDialog dialog = optionPane.createDialog("Ошибка");
                        dialog.setVisible(true);
                    }
                }
            });
        queryArea.setLineWrap(true);
        queryArea.setWrapStyleWord(true);
        JPanel full = new JPanel();
        JPanel gr = new JPanel();
        gr.setPreferredSize(new Dimension(600, 70));
        gr.add(discardButton);
        gr.add(saveButton);
        full.add(queryArea);
        full.add(gr);
        full.setLayout(new BoxLayout(full,BoxLayout.Y_AXIS));
        d2.add(full);
        d2.setSize(600,300);
        d2.setVisible(true);
    }

    private void delDT(){
        JDialog d3 = new JDialog(DB_Work.this, "-DELETE- SQL QUERY");
            JTextArea queryArea = new JTextArea("DELETE FROM декларации WHERE");
            d3.setModal(true);
            discardButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    d3.dispose();
                }
            });
            saveButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try{
                        PreparedStatement p = DatabaseConnection.getInstance().getConnection().prepareStatement(queryArea.getText());
                        p.executeUpdate();
                        JOptionPane.showMessageDialog(null, "Успешно", "Изменено", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception e1) {
                        JScrollPane scroll = new JScrollPane(new JLabel("Произошла ошибка: " + e.toString()));
                        JOptionPane optionPane = new JOptionPane(scroll, JOptionPane.ERROR_MESSAGE);
                        optionPane.setMaximumSize(new Dimension(400, 300));
                        JDialog dialog = optionPane.createDialog("Ошибка");
                        dialog.setVisible(true);
                        // JOptionPane.showMessageDialog(null, scroll, "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
        queryArea.setLineWrap(true);
        queryArea.setWrapStyleWord(true);
        JPanel full = new JPanel();
        JPanel gr = new JPanel();
        gr.setPreferredSize(new Dimension(600, 70));
        gr.add(discardButton);
        gr.add(saveButton);
        full.add(queryArea);
        full.add(gr);
        full.setLayout(new BoxLayout(full,BoxLayout.Y_AXIS));
        d3.add(full);
        d3.setSize(600,300);
        d3.setVisible(true);
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DB_Work app = new DB_Work();
            app.setVisible(true);
        });
    }
}
