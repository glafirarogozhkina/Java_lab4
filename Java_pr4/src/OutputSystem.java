import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;

// Класс вывода симуляции на экран
public class OutputSystem extends JFrame {
    private JButton runButton; // Кнопка запуска симуляции
    private JSpinner floorsNumField; // Количество этажей
    private JFormattedTextField  minIntervField; // Минимальный интервал до появления пассажира
    private JFormattedTextField  maxIntervField; // Максимальный интервал до появления пассажира
    private JSpinner liftsNumField; // Количество лифтов
    private JSpinner maxLiftCapacityField; // Максимальная вместимость лифта
    JPanel panel;
    private JTextArea output; // Поле вывода симуляции
    private JTextArea timerArea; // Поле вывода таймера
    private int timer; // Таймер
    private LiftController controller; // Система управления лифтами

    public OutputSystem() {
        super("Система управления лифтами");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 800); // размеры окна
        setLocationRelativeTo(null); // центрировать окно на экране

        initView(); // метод для создания элементов управления на форме

        setVisible(true);
    }

    // создаем элементы управления
    private void initView() {
        // инициализация
        runButton = new JButton("Запустить");
        runButton.addActionListener(e -> run());
        floorsNumField = new JSpinner(new SpinnerNumberModel(15, 10, 30, 1));
        minIntervField = new JFormattedTextField(NumberFormat.getIntegerInstance());
        minIntervField.setColumns(10);
        minIntervField.setValue(3); // начальное значение поля
        maxIntervField = new JFormattedTextField(NumberFormat.getIntegerInstance());
        maxIntervField.setColumns(10);
        maxIntervField.setValue(5); // начальное значение поля
        liftsNumField = new JSpinner(new SpinnerNumberModel(2, 2, 5, 1));
        maxLiftCapacityField = new JSpinner(new SpinnerNumberModel(8, 3, 12, 1));
        var title = new JLabel("Задайте параметры генерации\n");
        title.setFont(new Font("Arial", Font.PLAIN, 52));

        // добавление эелементов на форму
        panel = new JPanel();
        panel.add(title);
        panel.add(new JLabel("Количество этажей в доме: "));
        panel.add(floorsNumField);
        panel.add(new JLabel("\nМинимальный интервал паузы до появления нового пассажира (cек):"));
        panel.add(minIntervField);
        panel.add(new JLabel("\nМаксимальный интервал паузы до появления нового пассажира (cек):"));
        panel.add(maxIntervField);
        panel.add(new JLabel("Количество лифтов: "));
        panel.add(liftsNumField);
        panel.add(new JLabel("Максимальная вместимость лифта: "));
        panel.add(maxLiftCapacityField);
        panel.add(runButton);

        add(panel);
    }

    // симуляция запущена
    public void run() {
        if (Integer.parseInt(minIntervField.getText()) > Integer.parseInt(maxIntervField.getText())) {
            JOptionPane.showMessageDialog(null, "Интервал введен неверно");
            return;
        }

        panel.removeAll(); // очищаем форму
        remove(panel);
        // добавляем новые элементы
        panel = new JPanel();
        output = new JTextArea((int) floorsNumField.getValue(), (int)liftsNumField.getValue() * 8 + 10);
        output.setFont(new Font("Arial", Font.PLAIN, 16)); // изменяем размер шрифта на 16
        output.setEditable(false);
        timerArea = new JTextArea("Время работы симуляции:\t 0:0");
        timerArea.setEditable(false);
        panel.add(output);
        panel.add(timerArea);
        add(panel);
        setVisible(true);

        // создаем контроллер системы
        controller = new LiftController((int)floorsNumField.getValue(),
                Integer.parseInt(minIntervField.getText()) * 1000,
                Integer.parseInt(maxIntervField.getText()) * 1000,
                (int) liftsNumField.getValue(),
                (int) maxLiftCapacityField.getValue());

        // создаем поток для вывода информации о симуляции
        Thread outputThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                updateSystemState();
            }
        });
        outputThread.start();
    }

    // обновляем состояние симуляции
    public void updateSystemState() {
        var floorsState = updateFloors();
        String result = "";
        for (int i = controller.getFloors() - 1; i >= 0; --i)
            result += (i + 1) + ".\t" + floorsState[i] + "\n";
        output.setText(result);
        timer += 500;
        timerArea.setText("Время работы симуляции: " + ((timer / 1000 / 60) % 60) + ":" + ((timer / 1000) % 60));
    }

    // этажи в виде строк
    private String[] updateFloors() {
        int floors = controller.getFloors();
        String[] floorsState = new String[floors];

        for (int i = 0; i < floors; ++i) {
            floorsState[i] = "";
        }

        // выводим лифты
        var lifts = controller.getLifts();
        for (var lift : lifts) {
            int liftFloor = lift.getCurrentFloor();
            for (int i = 0; i < floors; ++i) {
                if (i == liftFloor - 1)
                    floorsState[i] += lift + " ";
                else floorsState[i] += "        ";
            }
        }

        // выводим запросы, принадлежащие лифтам
        for (var lift : lifts) {
            var liftRequests = lift.getRequests();
            for (var request : liftRequests) {
                Character destination = request.getDestination();
                floorsState[request.getInitialFloor() - 1] += "☺" + destination + " ";
            }
        }

        // выводим запросы из очереди ожидания
        for (var request : controller.getRequestsQuery()) {
            Character destination = request.getDestination();
            floorsState[request.getInitialFloor() - 1] += "☺" + destination + " ";
        }

        return floorsState;
    }
}