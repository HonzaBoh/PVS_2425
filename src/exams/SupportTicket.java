package exams;

public class SupportTicket {
    private int ticketId;
    private String employeeId;
    private String department;
    private String category;
    private String priority;
    private int createdHrsAgo;
    private String status;

    public SupportTicket(int ticketId, String employeeId, String department,
                         String category, String priority, int createdHrsAgo, String status) {
        this.ticketId = ticketId;
        this.employeeId = employeeId;
        this.department = department;
        this.category = category;
        this.priority = priority;
        this.createdHrsAgo = createdHrsAgo;
        this.status = status;
    }

    public int getTicketId() {
        return ticketId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getDepartment() {
        return department;
    }

    public String getCategory() {
        return category;
    }

    public String getPriority() {
        return priority;
    }

    public int getCreatedHrsAgo() {
        return createdHrsAgo;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return String.format(
                "Ticket{ID=%d, Emp=%s, Dept=%s, Cat=%s, Priority=%s, Created=%dh ago, Status=%s}",
                ticketId, employeeId, department, category, priority, createdHrsAgo, status
        );
    }
}