class GameChannel < ApplicationCable::Channel
  def subscribed
    @game = Game.find_by_id(params[:id])
    return reject_unauthorized_connection if not @game

    if @game.users == [current_user]
      @owner = true
    else
      current_user.update!(game: @game)
    end

    stream_for @game

    GameChannel.broadcast_to @game,
      type: 'players',
      phone_numbers: @game.users.reload.pluck(:phone_number)
  end

  def unsubscribed
    if @owner
      GameChannel.broadcast_to @game,
        type: 'end'
      @game.destroy
    else
      current_user.update!(game: nil)

      GameChannel.broadcast_to @game,
        type: 'players',
        phone_numbers: @game.users.reload.pluck(:phone_number)
    end
  end

  def ready
    GameChannel.broadcast_to @game,
      type: 'ready'
  end

  def start
    GameChannel.broadcast_to @game,
      type: 'start'
  end

  def added_picture
    GameChannel.broadcast_to @game,
      type: 'added_picture',
      who: current_user.phone_number
  end

  def used_picture
    GameChannel.broadcast_to @game,
      type: 'used_picture',
      who: current_user.phone_number
  end

  def used_magic_wand
    GameChannel.broadcast_to @game,
      type: 'used_magic_wand',
      who: current_user.phone_number
  end

  def win
    GameChannel.broadcast_to @game,
      type: 'win',
      who: current_user.phone_number
  end

  def lose
    GameChannel.broadcast_to @game,
      type: 'lose',
      who: current_user.phone_number
  end

  def message(data)
    GameChannel.broadcast_to @game,
      type: 'message',
      who: current_user.phone_number,
      body: data[:body]
  end
end
