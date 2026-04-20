package database;

import domain.Member;
import enums.AccountStatus;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * MemberDAO handles all database operations for members.
 */
public class MemberDAO {

    private Connection connection;

    public MemberDAO() {
        try {
            // get the single shared connection
            this.connection =
                DatabaseConnection.getInstance().getConnection();
        } catch (SQLException e) {
            System.out.println("MemberDAO connection error: "
                    + e.getMessage());
        }
    }

   
    /**
     * Inserts a new Member record into the members table.
     * @param member - Member object to insert into database
     */
    public void addMember(Member member) {
        String sql = "INSERT INTO members " +
                     "(name, email, phone, address, " +
                     "status, membership_date, " +
                     "max_borrow_limit) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement stmt =
                    connection.prepareStatement(sql);

            stmt.setString(1, member.getName());
            stmt.setString(2, member.getEmail());
            stmt.setString(3, member.getPhone());
            stmt.setString(4, member.getAddress());
            stmt.setString(5, member.getStatus().name());
            // convert java.util.Date to java.sql.Date
            stmt.setDate(6, member.getMembershipDate() != null
                    ? new Date(member.getMembershipDate()
                                     .getTime())
                    : null);
            stmt.setInt(7, member.getMaxBorrowLimit());

            stmt.executeUpdate();
            System.out.println("Member added successfully: "
                    + member.getName());

        } catch (SQLException e) {
            System.out.println("Error adding member: "
                    + e.getMessage());
        }
    }

    
    /**
     * Retrieves a single Member from the database by ID.
     * @param memberId - ID of the member to retrieve
     * @return         - Member object, or null if not found
     */
    public Member getMemberById(int memberId) {
        String sql = "SELECT * FROM members " +
                     "WHERE member_id = ?";

        try {
            PreparedStatement stmt =
                    connection.prepareStatement(sql);
            stmt.setInt(1, memberId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToMember(rs);
            } else {
                System.out.println(
                    "No member found with ID: " + memberId);
                return null;
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving member: "
                    + e.getMessage());
            return null;
        }
    }

    /**
     * Retrieves all members from the members table.
     * @return - List of all Member objects in the database
     */
    public List<Member> getAllMembers() {
        List<Member> members = new ArrayList<>();

        String sql = "SELECT * FROM members";

        try {
            PreparedStatement stmt =
                    connection.prepareStatement(sql);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                members.add(mapResultSetToMember(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving members: "
                    + e.getMessage());
        }
        return members;
    }

    /**
     * Updates an existing Member record in the members table.
     * @param member - Member object with updated information
     */
    public void updateMember(Member member) {
        String sql = "UPDATE members SET " +
                     "name = ?, "          +
                     "email = ?, "         +
                     "phone = ?, "         +
                     "address = ?, "       +
                     "status = ?, "        +
                     "max_borrow_limit = ? " +
                     "WHERE member_id = ?";

        try {
            PreparedStatement stmt =
                    connection.prepareStatement(sql);

            stmt.setString(1, member.getName());
            stmt.setString(2, member.getEmail());
            stmt.setString(3, member.getPhone());
            stmt.setString(4, member.getAddress());
            stmt.setString(5, member.getStatus().name());
            stmt.setInt(6, member.getMaxBorrowLimit());
            stmt.setInt(7, member.getUserId());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println(
                    "Member updated successfully.");
            } else {
                System.out.println(
                    "No member found to update.");
            }

        } catch (SQLException e) {
            System.out.println("Error updating member: "
                    + e.getMessage());
        }
    }

 
    /**
     * Updates only the status field of a member record.
     * @param memberId - ID of the member to update
     * @param status   - new AccountStatus to apply
     */
    public void updateMemberStatus(int memberId,
                                   AccountStatus status) {
        String sql = "UPDATE members SET status = ? " +
                     "WHERE member_id = ?";

        try {
            PreparedStatement stmt =
                    connection.prepareStatement(sql);

            stmt.setString(1, status.name());
            stmt.setInt(2, memberId);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println(
                    "Member status updated to: " + status);
            } else {
                System.out.println(
                    "No member found with ID: " + memberId);
            }

        } catch (SQLException e) {
            System.out.println(
                "Error updating member status: "
                    + e.getMessage());
        }
    }

    
    /**
     * Deletes a Member record from the members table by ID.
     * @param memberId - ID of the member to delete
     */
    public void removeMember(int memberId) {
        String sql = "DELETE FROM members " +
                     "WHERE member_id = ?";

        try {
            PreparedStatement stmt =
                    connection.prepareStatement(sql);
            stmt.setInt(1, memberId);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println(
                    "Member removed successfully.");
            } else {
                System.out.println(
                    "No member found with that ID.");
            }

        } catch (SQLException e) {
            System.out.println("Error removing member: "
                    + e.getMessage());
        }
    }

 
    /**
     * Searches the members table for members matching
     * the given name. Uses SQL LIKE for partial matching.
     * @param name - full or partial name to search for
     * @return     - List of matching Member objects
     */
    public List<Member> searchByName(String name) {
        List<Member> members = new ArrayList<>();

        String sql = "SELECT * FROM members " +
                     "WHERE name LIKE ?";

        try {
            PreparedStatement stmt =
                    connection.prepareStatement(sql);

            // wrap with % for partial matching
            stmt.setString(1, "%" + name + "%");

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                members.add(mapResultSetToMember(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error searching members: "
                    + e.getMessage());
        }
        return members;
    }

 
    /**
     * Maps a ResultSet row to a Member object.
     * @param rs - ResultSet row from database query
     * @return   - populated Member object
     * @throws SQLException if column name is wrong
     */
    private Member mapResultSetToMember(ResultSet rs)
            throws SQLException {
        return new Member(
            rs.getInt("member_id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("phone"),
            rs.getString("address"),
            // convert String from DB back to AccountStatus enum
            AccountStatus.valueOf(rs.getString("status")),
            rs.getDate("membership_date"),
            rs.getInt("max_borrow_limit")
        );
    }
}
