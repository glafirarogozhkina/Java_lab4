import java.util.ArrayList;
import java.util.List;

// Лифт
public class Lift {
    private int currentFloor; // Текущий этаж
    private LiftState state; // Состояние лифта (направление движения или остановка)
    private List<LiftRequest> requests; // Список заявок для перемещения лифта
    private List<LiftRequest> passangers; // Список пассажиров в лифте
    private int maxCapacity; // Максимальная вместимость
    private int currentLoad; // Текущая загрузка

    // Конструктор класса
    public Lift(int currentFloor, int maxCapacity) {
        this.currentFloor = currentFloor;
        this.state = LiftState.CLOSEDOORS;
        this.requests = new ArrayList<>();
        this.passangers = new ArrayList<>();
        this.maxCapacity = maxCapacity;
        this.currentLoad = 0;
    }

    // Метод для добавления нового пассажира в очередь
    public void addRequest(LiftRequest request) {
        this.requests.add(request);
    }

    // Основной цикл работы лифта
    public void operate() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(2000);
                if (!this.requests.isEmpty() || !this.passangers.isEmpty()) {
                    // Получаем ближайший этаж из списка
                    var request = getClosestRequest();

                    // Находим этаж, на который нужно ехать
                    int destination = request.getIsTaken() ? request.getDestinationFloor() : request.getInitialFloor();

                    // Перемещаемся на нужный этаж
                    moveToFloor(destination);

                    // Если приехали на нужный этаж, то подбираем либо высаживаем пассажира
                    if (!request.getIsTaken() && currentFloor == request.getInitialFloor()) {
                        passengerIn(request);
                    }
                    else if (request.getIsTaken() && currentFloor == request.getDestinationFloor()) {
                        passengerOut(request);
                    }
                } else {
                    // Если очередь пуста, переходим в режим ожидания
                    this.state = LiftState.CLOSEDOORS;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // Находим ближайший этаж на который нужно ехать
    private LiftRequest getClosestRequest() {
        int minDistance = Integer.MAX_VALUE;
        LiftRequest closestRequest = null;
        // Если вместимость позволяет, ищем среди ожидающих пассажиров
        if (canHold()) {
            for (var request : requests) {
                int liftDistance = Math.abs(currentFloor - request.getInitialFloor());
                if (liftDistance < minDistance) {
                    closestRequest = request;
                    minDistance = liftDistance;
                }
            }
        }

        //
        for (var passenger : passangers) {
            int liftDistance = Math.abs(currentFloor - passenger.getDestinationFloor());
            if (liftDistance < minDistance) {
                closestRequest = passenger;
                minDistance = liftDistance;
            }
        }

        return closestRequest;
    }

    // Метод для перемещения лифта на указанный этаж
    private void moveToFloor(int floor) {
        if (!Thread.currentThread().isInterrupted())
            // Определяем направление движения лифта
            this.state = (floor > currentFloor) ? LiftState.UP : LiftState.DOWN;
        // Перемещаемся на указанный этаж
        if (this.currentFloor != floor) {
            if (this.state == LiftState.UP) {
                this.currentFloor++;
            } else {
                this.currentFloor--;
            }
        }
    }

    // Подбираем пассажира
    private void passengerIn(LiftRequest request) throws InterruptedException {
        // Открываем двери
        openDoors();

        Thread.sleep(1000);
        ++currentLoad;
        request.Take();
        this.requests.remove(request);
        this.passangers.add(request);
        Thread.sleep(1000);

        // Закрываем двери
        closeDoors();
    }

    // Выпускаем пассажира
    private void passengerOut(LiftRequest request) throws InterruptedException {
        // Открываем двери
        openDoors();

        Thread.sleep(1000);
        --currentLoad;
        this.passangers.remove(request);
        Thread.sleep(1000);

        // Закрываем двери
        closeDoors();
    }

    // Метод для открытия дверей лифта
    private void openDoors() {
        this.state = LiftState.OPENDOORS;
    }

    // Метод для закрытия дверей лифта
    private void closeDoors() {
        this.state = LiftState.CLOSEDOORS;
    }

    public int getCurrentFloor() { return currentFloor; }
    public LiftState getState() { return state; }
    public List<LiftRequest> getRequests() { return requests; }

    public Boolean canHold() { return currentLoad < maxCapacity; } // Есть ли в лифте место

    // Состояние лифта в виде строки
    @Override
    public String toString() {
        String load = currentLoad == 0 ? "" : String.valueOf(currentLoad);
        if (state == LiftState.OPENDOORS) return "[  " + load + "  ]";
        else {
            String view = "[" + load + "]";
            if (state == LiftState.UP) view += "↑   ";
            else if (state == LiftState.DOWN) view += "↓   ";
            else view += "    ";
            return view;
        }
    }
}
