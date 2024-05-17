// Пассажиры
public class LiftRequest {
    private int initialFloor; // Этаж отправления
    private int destinationFloor; // Этаж назначения
    private Boolean isTaken; // Зашел ли пассажир в лифт

    // Конструктор класса
    public LiftRequest(int initialFloor, int destinationFloor) {
        this.initialFloor = initialFloor;
        this.destinationFloor = destinationFloor;
        this.isTaken = false;
    }

    public int getInitialFloor() { return initialFloor; }

    public int getDestinationFloor() { return destinationFloor; }
    public Boolean getIsTaken() {return isTaken;}
    public Character getDestination() { return (initialFloor < destinationFloor) ? '↑' : '↓'; }
    public void Take() {isTaken = true;} // Пассажир заходит в лифт
}
