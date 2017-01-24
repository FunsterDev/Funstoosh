class GameChannel < ApplicationCable::Channel
  def subscribed
    return reject_unauthorized_connection if not current_user
    @game = Game.find_by_id(params[:id])
    return reject_unauthorized_connection if not @game

    if @game.players == [current_user]
      @owner = true
    else
      current_user.update!(game: @game)
    end

    stream_from "game_#{@game.id}"

    broadcast_to "game_#{@game.id}",
      type: 'players',
      phone_numbers: @game.users.pluck(:phone_numbers)
  end

  def unsubscribed
    if @owner
      broadcast_to "game_#{@game.id}",
        type: 'end'
      @game.destroy
    else
      current_user.update!(game: nil)

      broadcast_to "game_#{@game.id}",
        type: 'players',
        phone_numbers: @game.users.pluck(:phone_numbers)
    end
  end

  def ready
    broadcast_to "game_#{@game.id}",
      type: 'ready'
  end

  def start
    broadcast_to "game_#{@game.id}",
      type: 'start'
  end

  def added_picture
    broadcast_to "game_#{@game.id}",
      type: 'added_picture',
      who: current_user.phone_number
  end

  def used_picture
    broadcast_to "game_#{@game.id}",
      type: 'used_picture',
      who: current_user.phone_number
  end

  def used_magic_wand
    broadcast_to "game_#{@game.id}",
      type: 'used_magic_wand',
      who: current_user.phone_number
  end

  def win
    broadcast_to "game_#{@game.id}",
      type: 'win',
      who: current_user.phone_number
  end

  def lose
    broadcast_to "game_#{@game.id}",
      type: 'lose',
      who: current_user.phone_number
  end

  def message(data)
    broadcast_to "game_#{@game.id}",
      type: 'message',
      who: current_user.phone_number,
      body: data[:body]
  end
end
